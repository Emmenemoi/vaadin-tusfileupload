package com.asaoweb.vaadin.uppyfileupload.client.domain;

import elemental.json.JsonObject;

import java.io.Serializable;

public class File implements Serializable {

    /**
     * Name of the plugin that was responsible for adding this file.
     * Typically a remote provider plugin like 'GoogleDrive' or a UI plugin like 'DragDrop'.
     */
    private String source;

    private String name;

    /**
     * Unique ID for the file.
     */
    private String id;

    /**
     * Object containing file metadata. Any file metadata should be JSON-serializable.
     */
    private JsonObject meta;

    /**
     * MIME type of the file. This may actually be guessed if a file type was not provided by the user’s browser,
     * so this is a best-effort value and not guaranteed to be accurate.
     */
    private String type;

    /**
     * An object with upload progress data.
     */
    private Progress progress;

    /**
     * Size in bytes of the file.
     */
    private Long size;

    /**
     * Is this file imported from a remote provider?
     */
    private Boolean isRemote;

    private String remote;

    /**
     * An optional URL to a visual thumbnail for the file.
     */
    private String preview;

    /**
     * When an upload is completed, this may contain a URL to the uploaded file.
     * Depending on server configuration it may not be accessible or accurate.
     */
    //private String uploadUrl;

    private String extension;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonObject getMeta() {
        return meta;
    }

    public void setMeta(JsonObject meta) {
        this.meta = meta;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Boolean getRemote() {
        return isRemote;
    }

    public void setRemote(Boolean remote) {
        isRemote = remote;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    //public String getUploadUrl() {
    //    return uploadUrl;
    //}

    //public void setUploadUrl(String uploadUrl) {
    //    this.uploadUrl = uploadUrl;
   // }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }
}
