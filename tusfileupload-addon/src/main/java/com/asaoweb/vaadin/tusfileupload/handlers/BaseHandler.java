package com.asaoweb.vaadin.tusfileupload.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asaoweb.vaadin.tusfileupload.Composer;
import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.Locker;
import com.asaoweb.vaadin.tusfileupload.TUSFileUploadHandler;
import com.asaoweb.vaadin.tusfileupload.data.Datastore;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;
import com.asaoweb.vaadin.fileupload.handlers.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.VaadinRequest;

public abstract class BaseHandler {
	private static final Logger log = LoggerFactory.getLogger(BaseHandler.class.getName());
	
	final Config config;
	final Locker locker;
	final Datastore datastore;
	final VaadinRequest request;
	final Response response;
	final StreamVariable streamVariable;

	public BaseHandler(Composer composer, VaadinRequest request, Response response, StreamVariable streamVariable) {
		this.config = composer.getConfig();
		this.locker = composer.getLocker();
		this.datastore = composer.getDatastore();
		this.request = request;
		this.response = response;
		this.streamVariable = streamVariable;
	}

	public abstract void go() throws Exception;

	/*
	 * Returns null if header doesn't exist or isn't a long value.
	 */
	public Long getLongHeader(String header) {
		String headerValue = request.getHeader(header);
		if (headerValue == null) {
			return null;
		}
		Long value = null;
		try {
			value = new Long(headerValue);
			log.debug("parsed header " + header + "=" + headerValue);
			return value;
		} catch (NumberFormatException ne) {
			log.debug(ne.toString());
			return null;
		}
	}

	public String getID() {
		return getID(request);
	}
	
	public void checkAuthSecurity() throws TusException {
		if (config.enforceAuthSecurity) {
			String username = TUSFileUploadHandler.getAuthenticatedUser(request);
			String id = getID();
			FileInfo fi;
			try {
				fi = datastore.getFileInfo(id);
			} catch (Exception e) {
				throw new TusException.Security(e.getMessage());
			}
			if (id != null && !id.isEmpty() && fi != null) {
				// if file owner is set, enforce owner check
				if (fi.username != null && !fi.username.isEmpty() && !fi.username.equals(username)) {
					throw new TusException.Security("Owner mismatch: "+fi.username+" for auth user "+username);
				}
			}
		}
	}
	
	/*
	 * Extract id from URL. If this returns null it means the URL was invalid.
	 * Caller should return 404 not found or BAD_REQUEST?.
	 */
	public static String getID(VaadinRequest vaadinRequest) {
		// Returns everything after the servlet path and before the query string (if
		// any)
		String uppUri = TUSFileUploadHandler.getUppURI(vaadinRequest);
		log.debug("uppUri is: {}", uppUri);
		if (uppUri == null) {
			return null;
		}
		String[] parts = uppUri.split("/");
		String idpart="";
		if (parts.length > TUSFileUploadHandler.UPLOAD_URL_FILE_ID_POSITION) {
			idpart = parts[TUSFileUploadHandler.UPLOAD_URL_FILE_ID_POSITION];
		}
			
		// Matches file ID consisting of letters, digits and underscores with optional
		// trailing slash
		Pattern pattern = Pattern.compile("(\\w+)");
		Matcher matcher = pattern.matcher(idpart);
		if (!matcher.matches()) {
			log.debug("URL doesn't have form of an upload endpoint.");
			return null;
		}
		String id = matcher.group(1);
		log.debug("file ID is:" + id);
		return id;
	}
	
}
