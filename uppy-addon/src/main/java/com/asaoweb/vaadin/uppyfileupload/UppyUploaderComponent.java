package com.asaoweb.vaadin.uppyfileupload;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.component.UploadComponent;
import com.asaoweb.vaadin.fileupload.events.Events;
import com.asaoweb.vaadin.uppyfileupload.client.UppyComponentServerRpc;
import com.asaoweb.vaadin.uppyfileupload.client.dashboard.AbstractDashboardParameters;
import com.asaoweb.vaadin.uppyfileupload.client.domain.UploadData;
import com.asaoweb.vaadin.uppyfileupload.client.UppyComponentClientRpc;
import com.asaoweb.vaadin.uppyfileupload.client.UppyUploaderComponentState;

import com.asaoweb.vaadin.uppyfileupload.client.domain.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gwt.json.client.JSONObject;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.SizeWithUnit;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// This is the server-side UI component that provides public API 
// for UppyComponent
@JavaScript({
        "vaadin://addons/uppy-addon/bundle.uppy.min.js?202304112"
        //"bundle.uppy.min.js"
})
public class UppyUploaderComponent extends UploadComponent {

    private final static Logger logger = Logger.getLogger(UppyUploaderComponent.class.getName());

    //protected Map<String, Object> metaProps = new HashMap<>();

    private String companionUrl;

    private final UppyComponentServerRpc serverRpc = new UppyComponentServerRpc() {
        @Override
        public void onFileAdded(JsonObject file) {
                FileInfo fi = new FileInfo(file.getString("id"), Double.valueOf(file.getNumber("size")).longValue(),
                    0L, file.getString("name"), file.getString("type"));
                this.addFileToQueue(fi);
        }

        private void addFileToQueue(FileInfo fi) {
            getUI().access(() -> {
                if (!queue.contains(fi.queueId)) {
                    queue.add(fi.queueId);
                    fireQueued(new Events.FileQueuedEvent(UppyUploaderComponent.this, fi));
                }
            });
        }

        @Override
        public void onProgressUpdated(int progress) {
            logger.log(Level.FINEST, "Full upload progress : " + progress + "%");
        }

        @Override
        public void onRestrictionFailed(JsonObject file, JsonObject error) {
            logger.log(Level.FINEST, "Restriction failed");
        }

        @Override
        public void onUploadProgressUpdated(JsonObject file, JsonObject progress) {
            FileInfo fi = new FileInfo(file.getString("id"), Double.valueOf(file.getNumber("size")).longValue(),
                    Double.valueOf(progress.getNumber("bytesUploaded")).longValue(), file.getString("name"), file.getString("type"));
            this.addFileToQueue(fi);
            //fireUpdateProgress(fi.offset, fi.entityLength);
            fireUpdateProgress(new Events.ProgressEvent(UppyUploaderComponent.this, fi));
        }

        @Override
        public void onUploadSuccess(JsonObject file, JsonObject response) {
            FileInfo fi = new FileInfo(
                file.getString("id"),
                Double.valueOf(file.getNumber("size")).longValue(),
                Double.valueOf(file.getNumber("size")).longValue(),
                file.getString("name"),
                file.getString("type"));
            addFileToQueue(fi);
            fi.offset=Double.valueOf(file.getNumber("size")).longValue();
            getUI().access(() -> {
                try {
                    if (response.hasKey("uploadURL")) {
                        fi.setUploadURL(new URI(response.getString("uploadURL")));
                    }
                    if (response.hasKey("preview")) {
                        fi.setPreview(new URI(response.getString("preview")));
                    }
                    queue.remove(fi.queueId);
                    fireUploadSuccess(new Events.SucceededEvent(UppyUploaderComponent.this, fi, queue.size()));
                } catch (Throwable ex) {
                    // TODO To process
                    logger.log(Level.WARNING,"onUploadSuccess failed: ", ex);
                }
            });

            if(autoResetAfterComplete) {
                clientRpc.resetDashboard();
            }
        }

        @Override
        public void onUploadComplete(JsonObject[] successfull, JsonObject[] failed) {
            logger.log(Level.FINEST, successfull.length + " files successfully uploaded ; " + failed.length + " files failed to upload.");
            getUI().access(() -> {
                Set<FileInfo> successFI = new HashSet<>();
                Set<FileInfo> failFI = new HashSet<>();
                for (JsonObject file : successfull) {
                    FileInfo fi = new FileInfo(
                        file.getString("id"),
                        Double.valueOf(file.getNumber("size")).longValue(),
                        Double.valueOf(file.getNumber("size")).longValue(),
                        file.getString("name"),
                        file.getString("type"));
                    try {
                        if (file.hasKey("uploadURL")) {
                            fi.setUploadURL(new URI(file.getString("uploadURL")));
                        }
                        if (file.hasKey("preview")) {
                            fi.setPreview(new URI(file.getString("preview")));
                        }
                    } catch (Exception ex) {
                        logger.log(Level.WARNING,"set uploadURL to "+file+" failed:", ex);
                    }
                    successFI.add(fi);
                }
                for (JsonObject file : failed) {
                    FileInfo fi = new FileInfo(file.getString("id"),
                        Double.valueOf(file.getNumber("size")).longValue(),
                        Double.valueOf(file.getObject("progress").getNumber("bytesUploaded")).longValue(),
                        file.getString("name"),
                        file.getString("type"));
                    try {
                        if (file.hasKey("uploadURL")) {
                            fi.setUploadURL(new URI(file.getString("uploadURL")));
                        }
                        if (file.hasKey("preview")) {
                            fi.setPreview(new URI(file.getString("preview")));
                        }
                    } catch (Exception ex) {
                        logger.log(Level.WARNING,"set uploadURL to "+file+" failed:", ex);
                    }
                    failFI.add(fi);
                }
                try {
                    fireUploadComplete(new Events.CompleteEvent(UppyUploaderComponent.this, successFI, failFI));
                } catch (Throwable ex) {
                    // TODO To process
                    logger.log(Level.WARNING, "onUploadComplete failed: ", ex);
                }
            });
        }

        @Override
        public void onUploadError(JsonObject file, JsonObject error, JsonObject response) {
            logger.log(Level.FINEST, "File " + file.getClass().getName() + " failed to upload.");
            getUI().access(() -> {
                FileInfo fi = new FileInfo(file.getString("id"), 0L,
                    0L, file.getString("name"), file.getString("type"));
                queue.remove(fi.queueId);
                fireFailed(new Events.FailedEvent(UppyUploaderComponent.this, fi, new Exception(error.getString("error"))));
                hasUploadInProgress = false;
            });
        }

        @Override
        public void onUploadStarted(UploadData data) {
            logger.log(Level.FINEST, "Upload of id " + data.getId() + " started to upload " + data.getFileIDs().length + " files.");
        }

    };
    private final UppyComponentClientRpc clientRpc;

    private boolean autoResetAfterComplete = true;

    public UppyUploaderComponent(Map<String, Object> meta, String companionUrl) {
        this(meta, companionUrl, null, false, "550px");
    }
    public UppyUploaderComponent(Map<String, Object> meta, String companionUrl, List<AbstractDashboardParameters.DashboardPlugin> plugins, boolean transferProgress, String dashboardHeight) {
        super();
        this.companionUrl = companionUrl;
        if(plugins != null) {
            getState(true).dashboardparameters.getPlugins().clear();
            getState(true).dashboardparameters.getPlugins().addAll(plugins);
        }
        setHeight(dashboardHeight);

        getState(true).setTransferProgress(transferProgress);

        getState(true).setDebug(logger.isLoggable(Level.FINE));

        getState(true).coreOptions.setMeta(toJson(meta));

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

    @Override
    protected UppyUploaderComponentState getState(boolean markDirty) {
        return (UppyUploaderComponentState) super.getState(markDirty);
    }

    public static Map<String, Object> toMap (Serializable t) {

        try {
            Map<String, Object> p = new HashMap<>();
            if(t != null) {
                BeanInfo beanInfo = Introspector.getBeanInfo(t.getClass());

                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    String name = pd.getName();
                    Object o = pd.getReadMethod().invoke(t);
                    if (o != null)
                        p.put(name, o);
                }
            }
            return p;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static JsonObject toJson (Map<String, Object> metaProps) {
        JsonObject metasJson = Json.createObject();
        if (metaProps != null) {
            for (Map.Entry<String, Object> entry : metaProps.entrySet()) {
                metasJson.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return metasJson;
    }


    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);
        if (unit == Unit.PIXELS) {
            getState(true).dashboardparameters.setHeight((int)height);
        }
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        getState(true).dashboardparameters.setThumbnailWidth(thumbnailWidth);
    }

    public void setHideProgressAfterFinish(boolean value){
        getState(true).dashboardparameters.setHideProgressAfterFinish(value);
    }

    // See hideSelector
    /*
    public void setShowSelectedFiles(boolean value){
        getState(true).dashboardparameters.setShowSelectedFiles(value);
    }*/

    public void setDisableThumbnailGenerator(boolean value){
        getState(true).dashboardparameters.setDisableThumbnailGenerator(value);
    }

    public void setAutoResetAfterComplete(boolean autoResetAfterComplete) {
        this.autoResetAfterComplete = autoResetAfterComplete;
    }

    @Override
    public void attach() {
        super.attach();
        if (companionUrl != null) {
            getState(true).setCompanionUrl(companionUrl);
        }
        setSizeFull();
        clientRpc.initInline(getState());
    }

    @Override
    public void removeFromQueue(String fileId) {
        clientRpc.removeFile(fileId);
    }

    @Override
    public void setRemainingQueueSeats(long remainingQueueSeats) {

    }

    public Collection<AbstractDashboardParameters.DashboardPlugin> getEnabledPlugins() {
        return getState().dashboardparameters.getPlugins();
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
        getState(true).coreOptions.restrictions.setMaxNumberOfFiles(maxFileCount);
        setMultiple(maxFileCount > 1);
    }

    @Override
    public void setMaxFileSize(long maxFileSize) {
        getState(true).coreOptions.restrictions.setMaxFileSize(maxFileSize);
    }

    @Override
    public void setMultiple(boolean multiple) {
        getState(true).coreOptions.setAllowMultipleUploads(multiple);
    }

    @Override
    public boolean isMultiple() {
        return getState(true).coreOptions.isAllowMultipleUploads();
    }

    @Override
    public void setClientSideDebug(boolean debugMode) {
        getState(true).coreOptions.setDebug(debugMode);
    }

    @Override
    public void setAcceptFilter(Collection<String> filters) {
        getState(true).coreOptions.restrictions.setAllowedFileTypes(filters);
    }

    public void setTransfertProgress(boolean transfertProgress){
        getState(true).setTransferProgress(transfertProgress);
    }

    public boolean isTransfertProgress() {
        return getState().isTransferProgress();
    }

    public void removeFile(String id) {
        clientRpc.removeFile(id);
        queue.remove(id);
    }

    @Override
    public void abortAll() {
        clientRpc.cancelAll();
        queue.clear();
    }

    @Override
    public void setButtonCaption(String caption) {
        getState(true).dashboardparameters.putLocale("dropPasteImport", caption);
        getState(true).dashboardparameters.putLocale("dropPaste", caption);
        getState(true).dashboardparameters.putLocale("dropHint", caption);
    }

    public void setMeta(Map<String, Object> meta) {
        if (isAttached()) {
            clientRpc.setMeta(toJson(meta));
        } else {
            getState(true).coreOptions.setMeta(toJson(meta));
        }
    }

    public JsonObject getMeta() {
        return getState().coreOptions.getMeta();
    }

    public void setDomain(String domain) {
        getState(true).coreOptions.setEdomain(domain);
    }

    public void hideSelector() {
        getState(true).dashboardparameters.setShowSelectedFiles(false);
        //getState().dashboardparameters.setDisableThumbnailGenerator(true);
        //getState().dashboardparameters.setDisableStatusBar(true);
    }

}
