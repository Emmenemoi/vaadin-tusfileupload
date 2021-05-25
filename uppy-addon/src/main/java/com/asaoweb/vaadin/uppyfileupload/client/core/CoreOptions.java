package com.asaoweb.vaadin.uppyfileupload.client.core;

import elemental.json.JsonObject;

import java.util.Collection;

public class CoreOptions {

    /**
     * https://uppy.io/docs/uppy/#id-39-uppy-39
     *
     * A site-wide unique ID for the instance.
     *
     * If multiple Uppy instances are being used, for instance, on two different pages, an id should be specified. This allows Uppy to store information in localStorage without colliding with other Uppy instances.
     *
     * Note that this ID should be persistent across page reloads and navigation—it shouldn’t be a random number that is different every time Uppy is loaded.
     * For example, if one Uppy instance is used to upload user avatars, and another to add photos to a blog post, you might use:
     *
     * <pre>{@code
     * const avatarUploader = new Uppy({ id: 'avatar' })
     * const photoUploader = new Uppy({ id: 'post' })
     * }</pre>
     */
    private String id = "uppy";

    private boolean autoProceed = true;

    private boolean allowMultipleUploads = true;

    private boolean debug = false;

    public final CoreRestrictions restrictions = new CoreRestrictions();

    private JsonObject meta;

    private String edomain = "EN";

    public CoreOptions() {
        // For auto instanciation only
        super();
    }


    public static class CoreRestrictions {

        private Long maxFileSize = null;
        private Long minFileSize = null;
        private Long maxTotalFileSize = null;
        private Integer maxNumberOfFiles = null;
        private Integer minNumberOfFiles = null;
        private Collection<String> allowedFileTypes = null;

        public Long getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(Long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public Long getMinFileSize() {
            return minFileSize;
        }

        public void setMinFileSize(Long minFileSize) {
            this.minFileSize = minFileSize;
        }

        public Long getMaxTotalFileSize() {
            return maxTotalFileSize;
        }

        public void setMaxTotalFileSize(Long maxTotalFileSize) {
            this.maxTotalFileSize = maxTotalFileSize;
        }

        public Integer getMaxNumberOfFiles() {
            return maxNumberOfFiles;
        }

        public void setMaxNumberOfFiles(Integer maxNumberOfFiles) {
            this.maxNumberOfFiles = maxNumberOfFiles;
        }

        public Integer getMinNumberOfFiles() {
            return minNumberOfFiles;
        }

        public void setMinNumberOfFiles(Integer minNumberOfFiles) {
            this.minNumberOfFiles = minNumberOfFiles;
        }

        public Collection<String> getAllowedFileTypes() {
            return allowedFileTypes;
        }

        public void setAllowedFileTypes(Collection<String> allowedFileTypes) {
            this.allowedFileTypes = allowedFileTypes;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAutoProceed() {
        return autoProceed;
    }

    public void setAutoProceed(boolean autoProceed) {
        this.autoProceed = autoProceed;
    }

    public boolean isAllowMultipleUploads() {
        return allowMultipleUploads;
    }

    public void setAllowMultipleUploads(boolean allowMultipleUploads) {
        this.allowMultipleUploads = allowMultipleUploads;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public JsonObject getMeta() {
        return meta;
    }

    public String getEdomain() {
        return edomain;
    }

    public void setEdomain(String edomain) {
        this.edomain = edomain;
    }

    public void setMeta(JsonObject meta) {
        this.meta = meta;
    }


}
