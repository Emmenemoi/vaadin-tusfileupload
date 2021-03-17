package com.asaoweb.vaadin.uppyfileupload.client.domain;

import java.io.Serializable;

public class Progress implements Serializable {

    /**
     * Number of bytes uploaded so far.
     */
    private Long bytesUploaded;

    /**
     * Number of bytes that must be uploaded in total.
     */
    private Long bytesTotal;

    /**
     * Null if the upload has not started yet. Once started, this property contains a UNIX timestamp.
     * Note that this is only set after preprocessing.
     */
   //private Long uploadStarted;

    /**
     * Boolean indicating if the upload has completed. Note this does not mean that postprocessing has completed, too.
     */
  // private Boolean uploadCompleted;

    /**
     * Integer percentage between 0 and 100.
     */
  // private Integer percentage;

    public Long getBytesUploaded() {
        return bytesUploaded;
    }

    public void setBytesUploaded(Long bytesUploaded) {
        this.bytesUploaded = bytesUploaded;
    }

    public Long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(Long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

   /* public Long getUploadStarted() {
        return uploadStarted;
    }

    public void setUploadStarted(Long uploadStarted) {
        this.uploadStarted = uploadStarted;
    }

    public Boolean getUploadCompleted() {
        return uploadCompleted;
    }

    public void setUploadCompleted(Boolean uploadCompleted) {
        this.uploadCompleted = uploadCompleted;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }*/
}
