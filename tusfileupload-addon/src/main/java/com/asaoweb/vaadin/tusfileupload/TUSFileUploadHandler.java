package com.asaoweb.vaadin.tusfileupload;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException.ConfigError;
import com.asaoweb.vaadin.tusfileupload.handlers.BaseHandler;
import com.asaoweb.vaadin.tusfileupload.handlers.DeleteHandler;
import com.asaoweb.vaadin.tusfileupload.handlers.HeadHandler;
import com.asaoweb.vaadin.tusfileupload.handlers.OptionsHandler;
import com.asaoweb.vaadin.tusfileupload.handlers.PatchHandler;
import com.asaoweb.vaadin.tusfileupload.handlers.PostHandler;
import com.asaoweb.vaadin.tusfileupload.handlers.Response;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.UploadException;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.communication.FileUploadHandler;
import com.vaadin.ui.UI;

public class TUSFileUploadHandler extends FileUploadHandler {
	
	private static final Logger log = LoggerFactory.getLogger(TUSFileUploadHandler.class.getName());
	
	public static final String UPLOAD_URL_PREFIX = "APP/TUS-UPLOAD/";
	public static final int UPLOAD_URL_FILE_ID_POSITION = 4;
	
	private Config config;
	private Composer composer;
	private boolean doTermination;
	private boolean doCreation;
	private boolean shouldSetDestroyListener = true;
	
	public TUSFileUploadHandler() throws ConfigError {
		this(new Config());
	}
	
	public TUSFileUploadHandler(Config config) {
		log.debug("Initialize TUSFileUploadHandler");
		try
		{
			this.config = config;
			composer = new Composer(config);
			doTermination = composer.datastore.getExtensions().contains("termination");
			doCreation = composer.datastore.getExtensions().contains("creation");
		}
		catch(TusException.ConfigError se)
		{
			log.error("", se);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	@Override
    public boolean handleRequest(VaadinSession session, VaadinRequest vaadinRequest,
            VaadinResponse vaadinResponse) throws IOException {
		if (shouldSetDestroyListener) {
			vaadinRequest.getService().addServiceDestroyListener((e)->destroy());
		}
		
		String pathInfo = vaadinRequest.getPathInfo();
		if (pathInfo == null || !pathInfo.startsWith('/'+UPLOAD_URL_PREFIX)) {
			return false;
		}

        /*
         * URI pattern: APP/UPLOAD/[UIID]/[PID]/[NAME]/[SECKEY] See
         * #createReceiverUrl
         */
        // strip away part until the data we are interested starts
        String uppUri = getUppURI(vaadinRequest);
        // 0= UIid, 1= cid, 2= name, 3= sec key
        String[] parts = uppUri.split("/");
        String uiId = parts[0];
        String connectorId = parts[1];
        String variableName = parts[2];

        // These are retrieved while session is locked
        ClientConnector source;
        StreamVariable streamVariable;

        session.lock();
        try {
            UI uI = session.getUIById(Integer.parseInt(uiId));
            UI.setCurrent(uI);

            streamVariable = uI.getConnectorTracker()
                    .getStreamVariable(connectorId, variableName);
            String secKey = uI.getConnectorTracker().getSeckey(streamVariable);
            if (secKey == null || !secKey.equals(parts[3])) {
				log.error("Method " + vaadinRequest.getMethod() + " not allowed: bad secret key");
                // TODO Should rethink error handling
                return true;
            }

            source = uI.getConnectorTracker().getConnector(connectorId);
        
        } finally {
            session.unlock();
        }

        Response response = new Response(vaadinResponse);
		try
		{
			log.debug("TUS UPLOAD SERVLET " + vaadinRequest.getMethod() + " " + vaadinRequest.getPathInfo() + ". User = " +
				getAuthenticatedUser(vaadinRequest));

			checkVersion(vaadinRequest, response);
			handleFileUploadSecurity(session, streamVariable, variableName, variableName, source, variableName);
			
			String method = vaadinRequest.getMethod();
			if (method.equals("OPTIONS")) {
				new OptionsHandler(composer, vaadinRequest, response, streamVariable).go();
			} else if (method.equals("HEAD")) {
				new HeadHandler(composer, vaadinRequest, response, streamVariable).go();
			} else if (method.equals("PATCH")) {
				new PatchHandler(composer, vaadinRequest, response, streamVariable).go();
			} else if (method.equals("POST") && doCreation) {
				new PostHandler(composer, vaadinRequest, response, streamVariable).go();
			} else if (method.equals("DELETE") && doTermination) {
				new DeleteHandler(composer, vaadinRequest, response, streamVariable).go();
			} else {
				log.info("Method " + vaadinRequest.getMethod() + " not allowed.");
				throw new TusException.MethodNotAllowed();
			}
		}
		catch (TusException texc)
		{
			response.setStatus(texc.getStatus()).setText(texc.getText());
		}
		catch(Exception e)
		{
			log.error("", e);
			response.setStatus(500).setText((e.getMessage()  == null) ? 
				"Server Error" : "Server Error: " + e.getMessage());
		}
		send(vaadinRequest, response);
        
        return true;
    }        

	public void destroy()
	{
		try
		{
			composer.datastore.destroy();
		}
		catch(Exception e)
		{
			log.error("", e);
		}
	}
	
	/*
	User authentication, if needed, is handled outside of the servlet and the information 
	is passed to the servlet via the request.getUserPrincipal().  If a user has been 
	authenticated, request.getUserPrincipal will be non null will contain the user's name.   
	Usually, when using authentication, a filter is configured to prevent the servlet 
	from running if a user hasn't logged in..
	 */
	public static String getAuthenticatedUser(VaadinRequest request)
	{
		Principal principal = request.getUserPrincipal();
		if (principal != null)
		{
			return principal.getName();
		} 
		return null;
	}
	
	public static String getUppURI(VaadinRequest request)
	{
		int startOfData = request.getPathInfo()
                .indexOf(UPLOAD_URL_PREFIX)
                + UPLOAD_URL_PREFIX.length();
        String uppUri = request.getPathInfo().substring(startOfData);
		return uppUri;
	}
	
	private void checkVersion(VaadinRequest request, Response response)
			throws Exception
		{
			String clientVersion = request.getHeader("tus-resumable");
			if (!request.getMethod().equals("OPTIONS") && 
				(clientVersion == null || !clientVersion.equals(config.tusApiVersionSupported)))
			{
				throw new TusException.UnsupportedVersion();
			}
		}
	
	private void addAccessHeaders(VaadinRequest request, Response response)
	{
		String origin = request.getHeader("Origin");
		if (origin != null && origin.length() > 0)
		{
			response.setHeader("Access-Control-Allow-Origin", origin);
			if (request.getMethod().equals("OPTIONS"))
			{
				response.setHeader("Access-Control-Allow-Methods", 
					"POST, GET, HEAD, PATCH, DELETE, OPTIONS");
				response.setHeader("Access-Control-Allow-Headers", 
						"Origin, " +
						"X-Requested-With, " +
						"Content-Type, " +
						"Upload-Length, " +
						"Upload-Offset, " +
						"Tus-Resumable, " +
						"Upload-Metadata");
				response.setHeader("Access-Control-Max-Age", "86400");

			} else
			{
				response.setHeader("Access-Control-Expose-Headers", 
						"Upload-Offset, " +
						"Location, " +
						"Upload-Length, " +
						"Tus-Version, " +
						"Tus-Resumable, " +
						"Tus-Max-Size, " +
						"Tus-Extension, " +
						"Upload-Metadata");
			}
		}
	}
	
	private void send(VaadinRequest request, Response response)
			throws IOException
		{
			response.setHeader("Tus-Resumable", config.tusApiVersionSupported);
			response.setHeader("X-Content-Type-Options", "nosniff");
			addAccessHeaders(request, response);
			if (request.getMethod().equals("HEAD"))
			{
				response.setText("");
			}
			String body = response.getText();
			if (body.length() > 0)
			{
				body += "\n";
				response.setHeader("Content-Type", "text/plain; charset=utf-8");
				response.setHeader("Content-Length", Long.toString(body.length()));
			}
			response.setText(body);
			response.write();
		}
	
	protected void handleFileUploadSecurity(VaadinSession session,
            StreamVariable streamVariable,
            String filename, String mimeType, ClientConnector connector, String variableName)
            throws UploadException {
		
        session.lock();
        try {
            if (connector == null) {
                throw new UploadException(
                        "File upload ignored because the connector for the stream variable was not found");
            }
            if (!connector.isConnectorEnabled()) {
                throw new UploadException("Warning: file upload ignored for "
                        + connector.getConnectorId()
                        + " because the component was disabled");
            }
        } finally {
            session.unlock();
        }
    }
}
