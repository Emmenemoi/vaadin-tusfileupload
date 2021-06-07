package com.asaoweb.vaadin.uppyfileupload.client;

import com.asaoweb.vaadin.uppyfileupload.client.domain.File;
import com.asaoweb.vaadin.uppyfileupload.client.domain.UploadData;
import com.vaadin.shared.communication.ServerRpc;
import elemental.json.JsonObject;

// ServerRpc is used to pass events from client to server
public interface UppyComponentServerRpc extends ServerRpc {

    /**
     * Fired each time a file is added.
     */
    void onFileAdded(JsonObject file);

    /**
     * Fired each time the total upload progress is updated:
     * @param progress An integer (0-100) representing the total upload progress.
     */
    void onProgressUpdated(int progress);

    /**
     * Fired when a file violates certain restrictions when added.
     * This event is just providing another choice for those who want to customize the behavior of file upload restrictions.
     * @param file
     * @param error
     */
    void onRestrictionFailed(JsonObject file, JsonObject error);

    /**
     * Fired each time an individual file upload progress is available:
     * @param file The File Object for the file whose upload has progressed.
     * @param progress Progress object
     */
    void onUploadProgressUpdated(JsonObject file, JsonObject progress);

    /**
     * Fired each time a single upload is completed.
     * @param file The File Object that has just been fully uploaded.
     * @param response An object with response data from the remote endpoint. The actual contents depend on the uploader plugin that is used.
     *                 For @uppy/xhr-upload, the shape is:
     *
     *                 {@code
     *                      {
     *                          status, // HTTP status code (0, 200, 300)
     *                          body, // response body
     *                          uploadURL // the file url, if it was returned
     *                      }
     *                 }
     */
    void onUploadSuccess(JsonObject file, JsonObject response);

    /**
     * Fired when all uploads are complete.
     *
     * The result parameter is an object with arrays of successful and failed files, just like in uppy.upload()’s return value.
     * @param successfull Array of succesfully uploaded files
     * @param failed Array of file which failed to upload.
     */
    void onUploadComplete(JsonObject[] successfull, File[] failed);

    /**
     * Fired each time a single upload has errored.
     * @param file The File Object for the file whose upload has just errored.
     */
    void onUploadError(JsonObject file, JsonObject error, JsonObject response);

    /**
     * Fired when upload starts.
     * @param data data object consists of `id` with upload ID and `fileIDs` array with file IDs in current upload data: { id, fileIDs }
     */
    void onUploadStarted(UploadData data);

}
