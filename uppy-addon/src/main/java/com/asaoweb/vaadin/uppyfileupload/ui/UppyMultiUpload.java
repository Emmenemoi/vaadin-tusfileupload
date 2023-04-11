package com.asaoweb.vaadin.uppyfileupload.ui;

import com.asaoweb.vaadin.fileupload.component.UploadComponent;
import com.asaoweb.vaadin.fileupload.data.FileDataProvider;
import com.asaoweb.vaadin.fileupload.data.FileListComponentProvider;
import com.asaoweb.vaadin.fileupload.events.Events;
import com.asaoweb.vaadin.uppyfileupload.UppyUploaderComponent;
import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.fileupload.ui.MultiUploadLayout;
import com.asaoweb.vaadin.uppyfileupload.client.dashboard.AbstractDashboardParameters;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.SerializablePredicate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class UppyMultiUpload<FILES> extends MultiUploadLayout<FILES> {

    public UppyMultiUpload(Map<String, Object> metas, FileDataProvider<FILES> existingFiles,FileListComponentProvider<FILES> filelistItemComponentProvider,
                           boolean allowReorder, String companionUrl, String dashboardHeight) {
        this(metas, existingFiles, filelistItemComponentProvider, allowReorder, companionUrl, null, false, dashboardHeight);
    }

    public UppyMultiUpload(Map<String, Object> metas, FileDataProvider<FILES> existingFiles, FileListComponentProvider<FILES> filelistItemComponentProvider,
                           boolean allowReorder, String companionUrl, List<AbstractDashboardParameters.DashboardPlugin> plugins, boolean transferProgress, String dashboardHeight) {
        this(new UppyUploaderComponent(metas, companionUrl, plugins, transferProgress, dashboardHeight), existingFiles, filelistItemComponentProvider, allowReorder);
    }

    public UppyMultiUpload(UppyUploaderComponent component, FileDataProvider<FILES> existingFiles, FileListComponentProvider<FILES> filelistItemComponentProvider, boolean allowReorder ) {
        super(component, existingFiles, filelistItemComponentProvider, allowReorder);
        if (component.isTransfertProgress()) {
            getUploader().hideSelector();
        }
        //getUploader().setHideProgressAfterFinish(true);
    }

    @Override
    public void setUploaderLocation() {
        this.addComponents(listPanel, uploadButton, infobar);
        this.setHeight("540px");
        this.setExpandRatio(listPanel, 0.3f);
        this.setExpandRatio(uploadButton, 0.65f);
        this.setExpandRatio(infobar, 0.05f);
    }

    @Override
    public UppyUploaderComponent getUploader() {
        return (UppyUploaderComponent)uploadButton;
    }
}
