package com.asaoweb.vaadin.uppyfileupload.client.domain;

import java.io.Serializable;

public class UploadData implements Serializable {

    private String id;

    private String[] fileIDs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getFileIDs() {
        return fileIDs;
    }

    public void setFileIDs(String[] fileIds) {
        this.fileIDs = fileIds;
    }
}
