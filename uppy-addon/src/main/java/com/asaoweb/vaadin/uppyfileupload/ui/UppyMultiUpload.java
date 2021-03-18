package com.asaoweb.vaadin.uppyfileupload.ui;

import com.asaoweb.vaadin.fileupload.events.Events;
import com.asaoweb.vaadin.uppyfileupload.UppyUploaderComponent;
import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.fileupload.ui.MultiUploadLayout;

import java.io.Serializable;
import java.util.List;

public class UppyMultiUpload extends MultiUploadLayout {

    public UppyMultiUpload(Serializable metas, List<FileInfo> existingFiles, FileInfoThumbProvider provider,
                           boolean allowReorder, String companionUrl) {
        super(new UppyUploaderComponent(metas, companionUrl), existingFiles, provider, allowReorder);
        getUploader().hideSelector();
        addInternalDeleteClickListener(new Events.InternalDeleteClickListener() {
            @Override
            public void internalDeleteClick(Events.InternalDeleteClickEvent evt) {
                getUploader().removeFile(evt.getFileInfo().id);
            }
        });
    }

    @Override
    public void setUploaderLocation() {
        this.setHeight("540px");
        this.addComponents(listPanel, uploadButton, infobar);
        this.setExpandRatio(listPanel, 0.3f);
        this.setExpandRatio(uploadButton, 0.65f);
        this.setExpandRatio(infobar, 0.05f);
    }

    @Override
    public UppyUploaderComponent getUploader() {
        return (UppyUploaderComponent)uploadButton;
    }
}
