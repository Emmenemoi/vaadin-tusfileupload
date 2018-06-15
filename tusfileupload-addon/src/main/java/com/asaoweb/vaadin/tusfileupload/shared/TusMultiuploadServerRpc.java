package com.asaoweb.vaadin.tusfileupload.shared;

import com.vaadin.shared.communication.ServerRpc;

import elemental.json.JsonArray;

public interface TusMultiuploadServerRpc extends ServerRpc {
	  void onQueuedFile(String queueId, String name, String contentType, long contentLength);

	  void setNextQueuedFileIdAndStart(String queueId);

	  void onProgress(String id, String name, long uploadedBytes, long totalBytes);

	  void onFileUploaded(String id, String name, String contentType);
	  
	  void onError(String id, String name, String contentType, String errorReason);
	  
	  void onFileCountError(int newlyAddedFiles);
	  
	  void onFileSizeError(JsonArray fileArray);
	  
}
