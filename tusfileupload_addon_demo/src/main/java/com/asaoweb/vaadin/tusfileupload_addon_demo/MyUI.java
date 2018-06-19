package com.asaoweb.vaadin.tusfileupload_addon_demo;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.FileInfo;
import com.asaoweb.vaadin.tusfileupload.component.TusMultiUpload;
import com.asaoweb.vaadin.tusfileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.tusfileupload.events.Events.SucceededEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.SucceededListener;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException.ConfigError;
import com.asaoweb.vaadin.tusfileupload.ui.TusMultiUploadLayout;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("tusfileupload_addon_demo")
public class MyUI extends UI {
	  private static final Logger logger = LoggerFactory.getLogger(MyUI.class.getName());

    private SucceededListener uploadSucceededHandler;
    private FileInfoThumbProvider fileInfoThumbProvider;
    
    // store files in memory for testing
    final Map<String,byte[]> uploadedFiles = new HashMap<>();
    
    @Override
    protected void init(VaadinRequest vaadinRequest) {
    	
        final VerticalLayout layout = new VerticalLayout();
        
        // Show uploaded file in this placeholder
        final Image image = new Image("Last Uploaded Image");
        
        final TextField tf = new TextField("Chunk size (bytes)");
        
        uploadSucceededHandler = (SucceededEvent event) -> {
            Notification.show("Handler: " + event.getFilename() + " uploaded (" + event.getLength() + " bytes). ");
            try {
	            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	            int nRead;
	            byte[] data = new byte[1024];
	            while ((nRead = event.getInputStream().read(data, 0, data.length)) != -1) {
	                buffer.write(data, 0, nRead);
	            }
	         
	            buffer.flush();	            
	            uploadedFiles.put(event.getId(), buffer.toByteArray());
            } catch (IOException e) {
            	e.printStackTrace();
            }
            
            StreamResource sr = new StreamResource(new StreamSource() {

				@Override
				public InputStream getStream() {
					return new ByteArrayInputStream(uploadedFiles.get(event.getId()));
				}
            	
            },event.getFilename());
            
            image.setHeight("100px");
            image.setSource(sr);
        };
        
        fileInfoThumbProvider = (FileInfo f) -> {
        	byte[] b = uploadedFiles.get(f.id);
        	return b != null && f.suggestedFiletype.contains("image") ? new StreamResource(() -> new ByteArrayInputStream(b), "thumb-"+f.suggestedFilename) : null;
        };
        
		try {
			ArrayList<FileInfo> existingFiles = new ArrayList<>();
			// Add 5 test images as existing in list
			for (int i=0; i < 8; i++) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InputStream is = null;
				try {
				  URL url = new URL("https://picsum.photos/200/300?random");
				  URLConnection urlConn = url.openConnection();
				  urlConn.setConnectTimeout(1000);
				  urlConn.setReadTimeout(1000);
				  is = urlConn.getInputStream();
				  byte[] byteChunk = new byte[4096];
				  int n;

				  while ( (n = is.read(byteChunk)) > 0 ) {
				    baos.write(byteChunk, 0, n);
				  }
				  
				  FileInfo fi = new FileInfo("existing-"+i, baos.size(), baos.size(), "existing-"+i+".jpg", "image/jpeg");
				  existingFiles.add(fi);
				  uploadedFiles.put(fi.id, baos.toByteArray());
				  
				  if (is != null) { is.close(); }
				} catch (IOException e) {
				  e.printStackTrace ();
				}
				
			}
			TusMultiUploadLayout mfUpload = new TusMultiUploadLayout("Send new files", new Config(), existingFiles, fileInfoThumbProvider, true);
			mfUpload.addSucceededListener(uploadSucceededHandler);
			mfUpload.getUploader().setRetryOnNetworkLoss(true);
			mfUpload.getUploader().setClientSideDebug(true);
			mfUpload.addFileDeletedClickListener(evt -> {
				uploadedFiles.remove(evt.getFileInfo().id);
				existingFiles.remove(evt.getFileInfo());
			});
			mfUpload.addFileIndexMovedListener(evt -> {
				logger.debug("FileIndexMovedListener: {}", evt);
				int oldIndexNewRef = evt.getNewIndex() < evt.getOldIndex() ? evt.getOldIndex() + 1 : evt.getOldIndex();
				int newIndexNewRef = evt.getNewIndex() > evt.getOldIndex() ? evt.getNewIndex() + 1 : evt.getNewIndex();
				existingFiles.add(newIndexNewRef, evt.getFileInfo());
				existingFiles.remove(oldIndexNewRef);
				// to verify the new order: click refresh button
			});
			
	        tf.addValueChangeListener(e -> {
	        	try {
	        	mfUpload.setChunkSize(Long.valueOf(tf.getValue()));
	        	} catch (Exception ex) {
	        		Notification.show(ex.getMessage());
	        	}
	        });
	        tf.setValue(String.valueOf(1024*100));

	        Button refreshBtn = new Button("Refresh list");
	        refreshBtn.addClickListener(e -> mfUpload.refreshFileList());
	        CheckBox compactChk = new CheckBox("CompactLayout", false);
	        compactChk.addValueChangeListener(e-> {
	        	mfUpload.setCompactLayout(e.getValue());
	        	mfUpload.refreshFileList();
	        });
	        
	        layout.addComponents(image, tf, mfUpload, refreshBtn, compactChk);
		} catch (ConfigError e) {
			e.printStackTrace();
		}   
        
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    	
    	@Override
    	public void init(ServletConfig servletConfig) throws ServletException {
    		super.init(servletConfig);

    	}
    }
}
