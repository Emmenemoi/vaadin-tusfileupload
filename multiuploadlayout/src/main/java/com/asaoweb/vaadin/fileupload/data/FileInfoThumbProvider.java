package com.asaoweb.vaadin.fileupload.data;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.vaadin.server.Resource;

import java.io.Serializable;

public interface FileInfoThumbProvider extends Serializable {
	/**
	 * 
	 * @param fileInfo
	 * @return Resource for thumbnail or null of no thumb
	 */
	Resource getThumb(FileInfo fileInfo);
}
