package com.asaoweb.vaadin.tusfileupload.shared;

import com.vaadin.shared.communication.ClientRpc;

public interface TusMultiuploadClientRpc extends ClientRpc {
	  void submitUpload();
	  void pauseUpload();
	  void resumeUpload();
	  void abortUpload();

	  void removeFromQueue(String queueId);
}
