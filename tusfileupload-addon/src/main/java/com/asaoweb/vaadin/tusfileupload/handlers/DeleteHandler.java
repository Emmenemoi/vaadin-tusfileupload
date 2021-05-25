package com.asaoweb.vaadin.tusfileupload.handlers;

import com.asaoweb.vaadin.tusfileupload.Composer;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;
import com.asaoweb.vaadin.fileupload.handlers.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.VaadinRequest;

public class DeleteHandler extends BaseHandler {
	private static final Logger log = LoggerFactory.getLogger(DeleteHandler.class.getName());

	public DeleteHandler(Composer composer, VaadinRequest request, Response response, StreamVariable streamVariable) {
		super(composer, request, response, streamVariable);
	}

	@Override
	public void go() throws Exception {
		// Get file ID from url
		String id = getID();
		if (id == null) {
			log.debug("No file id found in patch url");
			throw new TusException.NotFound();
		}
		
		checkAuthSecurity();
		
		boolean locked = false;
		try {
			locked = locker.lockUpload(id);
			if (!locked) {
				log.info("Couldn't lock " + id);
				throw new TusException.FileLocked();
			}
			whileLocked(id);
		} finally {
			if (locked) {
				locker.unlockUpload(id);
			}
		}
	}

	private void whileLocked(String id) throws Exception {
		FileInfo fileInfo = datastore.getFileInfo(id);
		if (fileInfo == null) {
			log.debug("fileInfo not found for '" + id + "'");
			throw new TusException.NotFound();
		}
		datastore.terminate(id);
		response.setStatus(Response.NO_CONTENT);
	}

}
