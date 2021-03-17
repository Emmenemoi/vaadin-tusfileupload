package com.asaoweb.vaadin.uppyfileupload.ui;

import com.asaoweb.vaadin.uppyfileupload.UppyUploaderComponent;
import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.fileupload.ui.MultiUploadLayout;

import java.io.Serializable;
import java.util.List;

public class UppyMultiUpload extends MultiUploadLayout {

    public UppyMultiUpload(Serializable metas, List<FileInfo> existingFiles, FileInfoThumbProvider provider,
                           boolean allowReorder) {
        super(new UppyUploaderComponent(metas), existingFiles, provider, allowReorder);
        getUploader().hideSelector();
    }

    @Override
    public void setUploaderLocation() {
        this.setHeight("780px");
        this.addComponents(listPanel, uploadButton, infobar);
        this.setExpandRatio(listPanel, 0.35f);
        this.setExpandRatio(uploadButton, 0.55f);
        this.setExpandRatio(infobar, 0.1f);
    }

    @Override
    public UppyUploaderComponent getUploader() {
        return (UppyUploaderComponent)uploadButton;
    }
}
