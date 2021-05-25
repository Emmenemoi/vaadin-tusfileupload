package com.asaoweb.vaadin.tusfileupload.reciever;

import java.io.OutputStream;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.tusfileupload.data.Datastore;
import com.vaadin.ui.Upload;

public interface TusReceiver extends Upload.Receiver, Datastore {
	public OutputStream receiveUpload(FileInfo fileInfos);

}
