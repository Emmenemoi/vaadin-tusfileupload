package com.asaoweb.vaadin.uppyfileupload.client;

import com.vaadin.shared.communication.ClientRpc;
import elemental.json.JsonObject;

// ClientRpc is used to pass events from server to client
// For sending information about the changes to component state, use State instead
public interface UppyComponentClientRpc extends ClientRpc {

    /**
     * Init an inline uploader.
     */
    void initInline(UppyUploaderComponentState opt);

    /**
     * Init a modal uploader
     */
    void initModal(UppyUploaderComponentState opt);

    /**
     * Alters global meta object in state, the one that can be set in Uppy options and gets merged with all newly added files.
     * Calling setMeta will also merge newly added meta data with previously selected files.
     * @param data
     */
    void setMeta(JsonObject data);

    /**
     * Remove file
     * @param fileid
     */
    void removeFile(String fileid);

    void cancelAll();

    void resetDashboard();
}