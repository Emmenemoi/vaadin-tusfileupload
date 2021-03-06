package com.asaoweb.vaadin.tusfileupload.handlers;

import com.asaoweb.vaadin.tusfileupload.Composer;
import com.asaoweb.vaadin.fileupload.handlers.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.StreamVariable;
import com.vaadin.server.VaadinRequest;
 
/*
	The TUSD implementation says this about why it returns 200 instead of 204:
		Although the 204 No Content status code is a better fit in this case,
		since we do not have a response body included, we cannot use it here
		as some browsers only accept 200 OK as successful response to a
		preflight request. If we send them the 204 No Content the response
		will be ignored or interpreted as a rejection.
		For example, the Presto engine, which is used in older versions of
		Opera, Opera Mobile and Opera Mini, handles CORS this way.package org.tus.servlet.upload;
		 

*/
public class OptionsHandler extends BaseHandler 
{
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PostHandler.class.getName());

	public OptionsHandler(Composer composer, VaadinRequest request, Response response, StreamVariable streamVariable)
	{
		super(composer, request, response, streamVariable);
	}


	@Override
	public void go() throws Exception
	{
		if (datastore.getExtensions() != null)
		{
			response.setHeader("Tus-Extension", datastore.getExtensions());
		}
		if (config.maxSize > 0)
		{
			response.setHeader("Tus-Max-Size", Long.toString(config.maxSize));
		}
		response.setStatus(Response.OK);
	}
}
