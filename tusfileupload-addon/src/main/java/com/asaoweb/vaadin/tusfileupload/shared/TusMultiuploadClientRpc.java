package com.asaoweb.vaadin.tusfileupload.shared;

import com.vaadin.shared.communication.ClientRpc;

public interface TusMultiuploadClientRpc extends ClientRpc {
	  void inputClick();
	  
	  void submitUpload();
	  void pauseUpload();
	  void resumeUpload();
	  void abortUpload();
	  void abortAllUploads();

	  void removeFromQueue(String queueId);
}
