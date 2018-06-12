package com.asaoweb.vaadin.tusfileupload.data;

import com.asaoweb.vaadin.tusfileupload.FileInfo;
import com.vaadin.server.Resource;

public interface FileInfoThumbProvider {
	/**
	 * 
	 * @param fileInfo
	 * @return Resource for thumbnail or null of no thumb
	 */
	Resource getThumb(FileInfo fileInfo);
}
