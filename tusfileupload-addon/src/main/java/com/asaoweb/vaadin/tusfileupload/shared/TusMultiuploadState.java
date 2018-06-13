package com.asaoweb.vaadin.tusfileupload.shared;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class TusMultiuploadState extends JavaScriptComponentState {
	//public TusMultiuploadTusConfig config;
	public boolean debug = true;
	public boolean clientSideProgress = true;
	
	public String 	endpoint;
	public String 	fingerprint;
	public boolean 	resume = true;
	public long		chunkSize = 1024*1024*5; // 5 MB default chunks
	// retries 11 times each time waiting the respective ms
	public int[]	retryDelays = { 2000, 4000, 10000, 10000, 10000, 10000, 10000, 10000, 20000, 20000, 30000 };
	public boolean  removeFingerprintOnSuccess = true;
	public boolean  withCredentials = false;
	
	public boolean rebuild = false;
	public String buttonCaption;
	public String mimeAccept = "*/*";
	public boolean multiple = true;
	public long maxFileSize = 0;
	
}