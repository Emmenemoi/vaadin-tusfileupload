package com.asaoweb.vaadin.fileupload.shared;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class UploaderComponentState extends JavaScriptComponentState {

    private int[]	retryDelays = { 2000, 4000, 10000, 10000, 10000, 10000, 10000, 10000, 20000, 20000, 30000 };
    private long 	maxFileSize = 0;
    private int 		maxFileCount = 0;

    public int[] getRetryDelays() {
        return retryDelays;
    }

    public void setRetryDelays(int[] retryDelays) {
        this.retryDelays = retryDelays;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxFileCount() {
        return maxFileCount;
    }

    public void setMaxFileCount(int maxFileCount) {
        this.maxFileCount = maxFileCount;
    }
}
