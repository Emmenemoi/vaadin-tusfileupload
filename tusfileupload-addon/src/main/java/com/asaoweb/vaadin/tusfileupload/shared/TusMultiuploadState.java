package com.asaoweb.vaadin.tusfileupload.shared;

import com.asaoweb.vaadin.fileupload.shared.UploaderComponentState;
import com.vaadin.shared.ui.JavaScriptComponentState;

public class TusMultiuploadState extends UploaderComponentState {
	//public TusMultiuploadTusConfig config;
	public boolean debug = false;
	public boolean clientSideProgress = true;
	
	public String 	endpoint;
	public String 	fingerprint;
	public boolean 	resume = true;
	public long		chunkSize = 1024*1024*5; // 5 MB default chunks
	// retries 11 times each time waiting the respective ms
	public boolean  removeFingerprintOnSuccess = true;
	public boolean  retryOnNetworkLoss = false;
	public boolean  withCredentials = false;
	
	public boolean 	rebuild = false;
	public String 	buttonCaption;
	public String 	mimeAccept = "*/*";
	public boolean 	multiple = true;
	public long 		remainingQueueSeats = 0;

}