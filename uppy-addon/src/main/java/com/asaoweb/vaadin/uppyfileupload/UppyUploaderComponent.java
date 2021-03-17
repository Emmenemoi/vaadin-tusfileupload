package com.asaoweb.vaadin.uppyfileupload;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.component.UploadComponent;
import com.asaoweb.vaadin.fileupload.events.Events;
import com.asaoweb.vaadin.uppyfileupload.client.UppyComponentServerRpc;
import com.asaoweb.vaadin.uppyfileupload.client.domain.UploadData;
import com.asaoweb.vaadin.uppyfileupload.client.UppyComponentClientRpc;
import com.asaoweb.vaadin.uppyfileupload.client.UppyUploaderComponentState;

import com.asaoweb.vaadin.uppyfileupload.client.domain.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.annotations.JavaScript;
import elemental.json.Json;
import elemental.json.JsonObject;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// This is the server-side UI component that provides public API 
// for UppyComponent
@JavaScript({
        "bundle.uppy.js"
})
public class UppyUploaderComponent extends UploadComponent {

    private final static Logger logger = Logger.getLogger(UppyUploaderComponent.class.getName());

    private Map<String, Object> metaProps;

    ObjectMapper objectMapper = new ObjectMapper();

    private final UppyComponentServerRpc serverRpc = new UppyComponentServerRpc() {
        @Override
        public void onFileAdded(JsonObject file) {
            FileInfo fi = new FileInfo();
            fi.queueId = file.getString("id");
            fi.suggestedFilename = file.getString("name");
            fi.suggestedFiletype = file.getString("type");
            fi.entityLength = Double.valueOf(file.getNumber("size")).longValue();
            queue.add(fi.queueId);
            fireQueued(new Events.FileQueuedEvent(UppyUploaderComponent.this, fi));
        }

        @Override
        public void onProgressUpdated(int progress) {
            logger.log(Level.SEVERE, "Full upload progress : " + progress + "%");
        }

        @Override
        public void onRestrictionFailed(JsonObject file, JsonObject error) {
            logger.log(Level.SEVERE, "Restriction failed");
        }

        @Override
        public void onUploadProgressUpdated(JsonObject file, JsonObject progress) {
            FileInfo fi = new FileInfo();
            fi.queueId = file.getString("id");
            fi.suggestedFilename = file.getString("name");
            fi.offset = Double.valueOf(progress.getNumber("bytesUploaded")).longValue();
            fi.entityLength = Double.valueOf(progress.getNumber("bytesTotal")).longValue();
            fireUpdateProgress(fi.offset, fi.entityLength);
            fireUpdateProgress(new Events.ProgressEvent(UppyUploaderComponent.this, fi));
        }

        @Override
        public void onUploadSuccess(JsonObject file, JsonObject response) {
            FileInfo fi = new FileInfo();
            fi.queueId = file.getString("id");
            fi.suggestedFilename = file.getString("name");
            fi.suggestedFiletype = file.getString("type");
            try {
                fireUploadSuccess(new Events.SucceededEvent(UppyUploaderComponent.this, fi, null,
                        new URI(response.getString("uploadURL"))));
            } catch (Throwable ex) {
                // TODO To process
                ex.printStackTrace();
            }
        }

        @Override
        public void onUploadComplete(JsonObject[] successfull, File[] failed) {
            logger.log(Level.SEVERE, successfull.length + " files successfully uploaded ; " + failed.length + " files failed to upload.");

        }

        @Override
        public void onUploadError(JsonObject file, JsonObject error, JsonObject response) {
            logger.log(Level.SEVERE, "File " + file.getClass().getName() + " failed to upload.");
            FileInfo fi = new FileInfo();
            fi.queueId = file.getString("id");
            fi.suggestedFilename = file.getString("name");
            fi.suggestedFiletype = file.getString("type");
            queue.remove(fi.queueId);
            fireFailed(new Events.FailedEvent(UppyUploaderComponent.this, fi, new Exception(error.getString("error"))));
            hasUploadInProgress = false;
        }

        @Override
        public void onUploadStarted(UploadData data) {
            logger.log(Level.SEVERE, "Upload of id " + data.getId() + " started to upload " + data.getFileIDs().length + " files.");
        }
    };
    private final UppyComponentClientRpc clientRpc;

    public UppyUploaderComponent(Serializable meta) {
        super();
        if (meta != null) {
            metaProps = toMap(meta);
        }
        addStyleName("v-panel");
        registerRpc(serverRpc);
        clientRpc = getRpcProxy(UppyComponentClientRpc.class);
        // To receive events from the client, we register ServerRpc
        //UppyComponentServerRpc rpc = this::handleClick;
        //registerRpc(rpc);
    }

    // We must override getState() to cast the state to UppyComponentStates
    @Override
    protected UppyUploaderComponentState getState() {
        return (UppyUploaderComponentState) super.getState();
    }

    public static Map<String, Object> toMap (Serializable t) {

        try {

            BeanInfo beanInfo = Introspector.getBeanInfo(t.getClass());
            Map p = new HashMap();

            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                String name = pd.getName();
                Object o = pd.getReadMethod().invoke(t);
                if (o != null)
                    p.put(name, o);
            }
            return p;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void attach() {
        super.attach();
        setSizeFull();
        clientRpc.initInline();
        JsonObject metasJson = Json.createObject();
        if (metaProps != null) {
            for (Map.Entry<String, Object> entry : metaProps.entrySet()) {
                metasJson.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        clientRpc.setMeta(metasJson);
    }

    @Override
    public void removeFromQueue(String fileId) {
        clientRpc.removeFile(fileId);
    }

    @Override
    public void setRemainingQueueSeats(int remainingQueueSeats) {

    }

    @Override
    public Long getMaxFileSize() {
        if (getState().coreOptions != null) {
            return getState().coreOptions.restrictions.getMaxFileSize();
        }
        return null;
    }

    @Override
    public Integer getMaxFileCount() {
        if (getState().coreOptions != null) {
            return getState().coreOptions.restrictions.getMaxNumberOfFiles();
        }
        return null;
    }

    @Override
    public void setChunkSize(long chunkSize) {
        // Managed companion side.
    }

    @Override
    public void setWithCredentials(boolean withCredentials) {

    }

    @Override
    public void setMaxFileCount(int maxFileCount) {
        getState().coreOptions.restrictions.setMaxNumberOfFiles(maxFileCount);
    }

    @Override
    public void setMaxFileSize(long maxFileSize) {
        getState().coreOptions.restrictions.setMaxFileSize(maxFileSize);
    }

    @Override
    public void setClientSideDebug(boolean debugMode) {
        getState().coreOptions.setDebug(debugMode);
    }

    @Override
    public void setAcceptFilter(Collection<String> filters) {
        getState().coreOptions.restrictions.setAllowedFileTypes(filters);
    }

    public void setDomain(String domain) {
        getState().coreOptions.setEdomain(domain);
    }

    public void hideSelector() {
        getState().dashboardparameters.setShowSelectedFiles(false);
        //getState().dashboardparameters.setDisableThumbnailGenerator(true);
        //getState().dashboardparameters.setDisableStatusBar(true);
    }

}
