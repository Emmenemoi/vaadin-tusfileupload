package com.asaoweb.vaadin.uppyfileupload.client;

import com.asaoweb.vaadin.uppyfileupload.client.core.CoreOptions;
import com.asaoweb.vaadin.uppyfileupload.client.dashboard.InlineDashboardParameters;
import com.vaadin.shared.ui.JavaScriptComponentState;

public class UppyUploaderComponentState extends JavaScriptComponentState {

    private int[] retryDelays = {2000, 4000, 10000, 10000, 10000, 10000, 10000, 10000, 20000, 20000, 30000};

    public final CoreOptions coreOptions = new CoreOptions();

    public InlineDashboardParameters dashboardparameters = new InlineDashboardParameters();

    /**
     * TODO Set in parameters the companion Url;
     */
    private String companionUrl = "http://localhost:3020";

    private boolean debug = false;

    private boolean transferProgress = false;

    private String uploadModule = "TUS";

    private boolean allowImageEditor = true;

    public UppyUploaderComponentState() {
    }

    public String getCompanionUrl() {
        return companionUrl;
    }

    public void setCompanionUrl(String companionUrl) {
        this.companionUrl = companionUrl;
    }

    public int[] getRetryDelays() {
        return retryDelays;
    }

    public void setRetryDelays(int[] retryDelays) {
        this.retryDelays = retryDelays;
    }

    public boolean isDebug() { return debug; }

    public void setDebug(boolean debug) { this.debug = debug; }

    public boolean isTransferProgress() { return transferProgress; }

    public void setTransferProgress(boolean transferProgress) { this.transferProgress = transferProgress; }

    public String getUploadModule() { return uploadModule; }

    public void setUploadModule(String uploadModule) { this.uploadModule = uploadModule; }

    public boolean isAllowImageEditor() {
        return allowImageEditor;
    }

    public void setAllowImageEditor(boolean allowImageEditor) {
        this.allowImageEditor = allowImageEditor;
    }
}