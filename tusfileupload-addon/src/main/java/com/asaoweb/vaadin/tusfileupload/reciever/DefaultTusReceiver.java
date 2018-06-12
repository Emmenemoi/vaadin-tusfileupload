package com.asaoweb.vaadin.tusfileupload.reciever;

import java.io.OutputStream;

import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.FileInfo;
import com.asaoweb.vaadin.tusfileupload.Locker;
import com.asaoweb.vaadin.tusfileupload.data.Store;
import com.vaadin.server.VaadinRequest;

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
