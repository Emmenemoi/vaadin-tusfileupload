package com.asaoweb.vaadin.tusfileupload.reciever;

import java.io.OutputStream;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.tusfileupload.data.Store;

public class DefaultTusReceiver extends Store implements TusReceiver {

	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		return null;
	}

	@Override
	public OutputStream receiveUpload(FileInfo fileInfos) {
		
		return null;
	}

}
