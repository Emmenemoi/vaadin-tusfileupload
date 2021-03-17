package com.asaoweb.vaadin.tusfileupload.handlers;

import com.asaoweb.vaadin.tusfileupload.Composer;
import com.asaoweb.vaadin.tusfileupload.TUSFileUploadHandler;
import com.asaoweb.vaadin.tusfileupload.events.StreamingEvents;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;
import com.asaoweb.vaadin.fileupload.handlers.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.VaadinRequest;

/*
	Todo: 
	- don't accept uploads when running out of disk space? (tusd uses a mix-in class for this)
	- deferredLength???  
*/
public class PostHandler extends BaseHandler 
{
	private static final Logger log = LoggerFactory.getLogger(PostHandler.class.getName());

	public PostHandler(Composer composer, VaadinRequest request, Response response, StreamVariable streamVariable)
	{
		super(composer, request, response, streamVariable);
	}

	@Override
	public void go() throws Exception
	{
		Long length = getLongHeader("upload-length");
		if (length == null || (long)length < 0)
		{
			throw new TusException.InvalidUploadLength();
		}
		if (config.maxSize > 0 && ((long)length > config.maxSize))
		{
			throw new TusException.MaxSizeExceeded();
		}

		// TODO: check if we have enough storage space?

		String metadata = request.getHeader("Upload-Metadata");

		// Generate unique id to serve as the file ID and store optional metadata.
		FileInfo fileInfo = new FileInfo((long)length, metadata, TUSFileUploadHandler.getAuthenticatedUser(request));

		datastore.create(fileInfo);

		String url = request.getPathInfo().toString(); 
		if (url.endsWith("/"))
		{
			url += fileInfo.id;
		} else
		{
			url += "/" + fileInfo.id;
		}
		log.debug("return url in location header.  url is " + url);

		response.setHeader("Location", url);
		response.setStatus(Response.CREATED);
		streamVariable.streamingStarted(new StreamingEvents.StreamingStartEventImpl(fileInfo));
	}


}
