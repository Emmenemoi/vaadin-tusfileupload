package com.asaoweb.vaadin.tusfileupload.component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;

import com.asaoweb.vaadin.fileupload.events.Events;
import com.asaoweb.vaadin.tusfileupload.TUSFileUploadHandler;
import com.asaoweb.vaadin.tusfileupload.data.Datastore;
import com.asaoweb.vaadin.tusfileupload.events.StreamingEvents;
import com.asaoweb.vaadin.fileupload.component.UploadComponent;
import com.asaoweb.vaadin.tusfileupload.Composer;
import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;
import com.asaoweb.vaadin.tusfileupload.shared.TusMultiuploadClientRpc;
import com.asaoweb.vaadin.tusfileupload.shared.TusMultiuploadServerRpc;
import com.asaoweb.vaadin.tusfileupload.shared.TusMultiuploadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.events.Events.FailedEvent;
import com.asaoweb.vaadin.fileupload.events.Events.FileQueuedEvent;
import com.asaoweb.vaadin.fileupload.events.Events.ProgressEvent;
import com.asaoweb.vaadin.fileupload.events.Events.StartedEvent;
import com.asaoweb.vaadin.fileupload.events.Events.SucceededEvent;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServletPortletHelper;
import com.vaadin.server.StreamVariable;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import elemental.json.JsonArray;
import elemental.json.JsonObject;


@SuppressWarnings("serial")
@JavaScript(value = {
		//"//rawgit.com/Emmenemoi/tus-js-client/master/dist/tus.js",
		"vaadin://addons/tusfileupload/tus.min.js",
		"vaadin://addons/tusfileupload/tusmultiupload-connector.js"})
public class TusMultiUpload extends UploadComponent {
	  
	  private static final Logger logger = LoggerFactory.getLogger(TusMultiUpload.class.getName());


	  /**
	   * The receiver registered with the upload component that all data will be
	   * streamed into.
	   */
	  protected Datastore receiverDataStore;
	  protected Config receiverConfig;

	protected Set<FileInfo> queueBatch = Collections.synchronizedSet(new HashSet<>());
	protected Set<FileInfo> queueFailedBatch = Collections.synchronizedSet(new HashSet<>());

	private final TusMultiuploadServerRpc serverRpc = new ServerRpcImpl();
	  private final TusMultiuploadClientRpc clientRpc;

	  private StreamVariable streamVariable;
	  private String currentQueuedFileId = "";
	  
	  /**
	   * Constructs the upload component.
	   * @throws TusException.ConfigError
	   */
	  public TusMultiUpload() throws TusException.ConfigError {
	    this(null, null);
	  }

	  /**
	   * Constructs the upload component.
	   *
	   * @param buttonCaption the caption of the component
	   * @param receiverConfig the receiver to create the output stream to receive upload
	   * data
	   * @throws TusException.ConfigError
	   */
	  public TusMultiUpload(String buttonCaption, Config receiverConfig) throws TusException.ConfigError {
	    registerRpc(serverRpc);
	    clientRpc = getRpcProxy(TusMultiuploadClientRpc.class);
	    if (receiverConfig == null) {
	    	receiverConfig = new Config();
	    }
	    setReceiverDatastore(receiverConfig);
	    setButtonCaption(buttonCaption);
		this.addStyleName("tusmultiupload");
	  }
	  
	  @Override
	  public void attach() {
	    super.attach();

	    // Generate the URL using the standard FileUploadHandler format and then
	    // replace the URL prefix with the prefix for our custom upload request
	    // handler. This ensures that the IDs and security key are properly
	    // generated and registered for our stream variable.
	    String url = getSession().getCommunicationManager().
	        getStreamVariableTargetUrl(this, "tusmultiupload", getStreamVariable());

	    // Replace the default upload URL prefix with the advanced upload request
	    // handler to be sure advanced stream variable events are generated.
	    url = url.replace(ServletPortletHelper.UPLOAD_URL_PREFIX,
	    		TUSFileUploadHandler.UPLOAD_URL_PREFIX);

	    getState().endpoint = url;
	    getState().rebuild = true;

	    try {
		    setMaxFileSize( receiverConfig.getLongValue("maxFileSize") );
			installHandler();
		} catch (TusException.ConfigError e) {
			e.printStackTrace();
		}
	  }

	  @Override
	  public void detach() {
	  	try {
			// Cleanup our stream variable.
			getUI().getConnectorTracker().cleanStreamVariable(getConnectorId(), "tusmultiupload");
		} catch (Exception e) {
	  		logger.error("Error cleaning the streaming variable for connector {}:", getConnectorId(), e);
		}
	    super.detach();
	  }
	  /**
	   * DOES NOT WORK
	  public void click() {
		  clientRpc.inputClick();
	  }*/
	  
	  /**
	   * Installs the {@link TUSFileUploadHandler} into the session if it is not
	   * already registered. This should be called when an TUS uploader is
	   * attached to the UI. It is safe to call this method multiple times and only
	   * a single handler will be installed for the session.
	   * @throws TusException.ConfigError
	   */
	  protected void installHandler() throws TusException.ConfigError {
	    // See if the uploader handler is already installed for this session.
	    boolean handlerInstalled = false;
	    for (RequestHandler handler : getSession().getRequestHandlers()) {
	      if (handler instanceof TUSFileUploadHandler) {
	        handlerInstalled = true;
	      }
	    }

	    // Install the upload handler if one is not already registered.
	    if (!handlerInstalled) {
	      getSession().addRequestHandler(new TUSFileUploadHandler(receiverConfig));
	    }
	  }

	  /**
	   * Returns the receiver that will be used to create output streams when a file
	   * starts uploading.
	   *
	   * @return the receiver for all incoming data
	   */
	  public Datastore getReceiverDataStore() {
	    return receiverDataStore;
	  }

	  /**
	   * Sets the receiver Datastore that will be used to create output streams when a file
	   * starts uploading.
	   *
	   * @param receiverConfig the receiver configuration to use for creating file output streams
	 * @throws Exception 
	   */
	  public void setReceiverDatastore(Config receiverConfig) throws TusException.ConfigError {
		  this.receiverConfig = receiverConfig;
		  Composer c;
			try {
				c = new Composer(receiverConfig);
			} catch (Exception e) {
				throw new TusException.ConfigError(e.getMessage());
			}
			setReceiverDatastore(c.getDatastore());
	  }
	  
	  private void setReceiverDatastore(Datastore receiverDataStore) {
		  this.receiverDataStore = receiverDataStore;
	  }

	  protected TusMultiuploadState getState() {
	    return (TusMultiuploadState) super.getState();
	  }

	  @Override
	  protected TusMultiuploadState getState(boolean markAsDirty) {
	    return (TusMultiuploadState) super.getState(markAsDirty);
	  }
	  
	  /**
	   * Returns the stream variable that will receive the data events and content.
	   *
	   * @return the stream variable for this component
	   */
	  public StreamVariable getStreamVariable() {
	    if (streamVariable == null) {
	      streamVariable = new StreamVariableImpl();
	    }

	    return streamVariable;
	  }
	  
	  /**
	   * Returns the caption displayed on the submit button.
	   *
	   * @return the caption of the submit button
	   */
	  public String getButtonCaption() {
	    return getState().buttonCaption;
	  }

	  /**
	   * Sets the caption displayed on the submit button or on the combination
	   * browse and submit button when in immediate mode. When not in immediate
	   * mode, the text on the browse button cannot be set.
	   *
	   * @param caption the caption of the submit button
	   */
	  @Override
	  public void setButtonCaption(String caption) {
	    getState().buttonCaption = caption;
	    getState().rebuild = true;
	  }
	  
	  /**
	   * Activates and receive the events on client side progress
	   *
	   * @param value should manage client side progress
	   */
	  public void setClientSideProgress(boolean value) {
		  getState().clientSideProgress = value;
	  }
	  
	  /**
	   * a number indicating the maximum size of a chunk uploaded 
	   * in a single request. Note that if the server has hard limits 
	   * (such as the minimum 5MB chunk size imposed by S3), 
	   * specifying a chunk size which falls outside those hard limits 
	   * will cause chunked uploads to fail.
	   *
	   * @param chunkSize set chunk size in bytes (0 for no limit)
	   */
	  @Override
	  public void setChunkSize(long chunkSize) {
		  getState().chunkSize = chunkSize;
	  }
	  
	  public long getChunkSize() {
		  return getState().chunkSize;
	  }
	  
	  /**
	   * withCredentials = false: a boolean which is be used as the value 
	   * for withCredentials in all XMLHttpRequests to use Cookies in requests. 
	   * The remote server must accept CORS and credentials
	   *
	   * @param withCredentials boolean allowing passing http credentials in requests
	   */
	  @Override
	  public void setWithCredentials(boolean withCredentials) {
		  getState().withCredentials = withCredentials;
	  }
	  
	  public boolean getWithCredentials() {
		  return getState().withCredentials;
	  }
	  
	  /**
	   * an array or null, indicating how many milliseconds should pass 
	   * before the next attempt to uploading will be started after the 
	   * transfer has been interrupted. The array's length indicates 
	   * the maximum number of attempts. For more details about the system 
	   * of retries and delays, read the Automated Retries section of 
	   * TUS Protocol
	   *
	   * @param retryDelays set the retry delays as an array of int or null
	   */
	  public void setRetryDelays(int[] retryDelays) {
		  getState().setRetryDelays(retryDelays);
	  }
	  
	  public int[] getRetryDelays() {
		  return getState().getRetryDelays();
	  }

	  @Override
	  public void setAcceptFilter(Collection<String> filter) {
	  	if (filter != null) {
			getState().mimeAccept = String.join(",", filter);
		}
	    getState().rebuild = true;
	  }
	  
	  public void setMultiple(boolean multiple) {
		  getState().multiple = multiple;
	  }
	  
	  public boolean isMultiple() {
		  return getState().multiple;
	  }
	  
	  public void setClientSideDebug(boolean clientSideDebug) {
		  getState().debug = clientSideDebug;
	  }
	  
	  public void setRetryOnNetworkLoss(boolean retryOnNetworkLoss) {
		  getState().retryOnNetworkLoss = retryOnNetworkLoss;
	  }
	  
	  public void abortAll() {
		  clientRpc.abortAllUploads();
		  queue.clear();
	  }

	  @Override
	  public void removeFromQueue(String queueId) {
		  if (queueId != null) {
			  if (queueId.equals(currentQueuedFileId)) {
				  clientRpc.abortUpload();
			  } else {
				  clientRpc.removeFromQueue(queueId);
			  }
			  queue.remove(queueId);
		  }
	  }

	  @Override
	  public void setMaxFileSize(long maxFileSize) {
		  getState().setMaxFileSize(maxFileSize);
	  }

	  @Override
	  public Long getMaxFileSize() {
		  return getState().getMaxFileSize();
	  }

	  @Override
	  public void setMaxFileCount(int maxFileCount) {
		  if (maxFileCount == 1) {
			  this.setMultiple(false);
		  } else {
			  this.setMultiple(true);			  
		  }
		  getState().setMaxFileCount(maxFileCount);
		  getState().remainingQueueSeats = maxFileCount;
	  }

	  @Override
	  public Integer getMaxFileCount() {
		  return getState().getMaxFileCount();
	  }

	  @Override
	  public void setRemainingQueueSeats(long remainingQueueSeats) {
		  remainingQueueSeats = Math.max(0,  remainingQueueSeats);
		  if (remainingQueueSeats <= getMaxFileCount()) {
			  getState().remainingQueueSeats = remainingQueueSeats;	
		  }
	  }
	  
	  public long getRemainingQueueSeats() {
		  return getState().remainingQueueSeats;
	  }

	  @Override
	  public int getQueueCount() {
		return queue.size();
	}

	  /******** PRIVATE CLASS ************/
	  /**
	   * The remote procedure call interface which allows calls from the client side
	   * to the server.
	   */
	  private class ServerRpcImpl implements TusMultiuploadServerRpc {

		@Override
		public void onQueuedFile(String queueId, String name, String contentType, long contentLength) {
			FileInfo fi = new FileInfo();
			fi.queueId = queueId;
			fi.suggestedFilename = name;
			fi.suggestedFiletype = contentType;
			fi.entityLength = contentLength;
			queue.add(queueId);
			logger.debug("onQueuedFile(ui) for file info {}", fi);
			fireQueued(new FileQueuedEvent(TusMultiUpload.this, fi));				
		}

		@Override
		public void onError(String queueId, String name, String contentType, String errorReason) {
			FileInfo fi = new FileInfo();
			fi.queueId = queueId;
			fi.suggestedFilename = name;
			fi.suggestedFiletype = contentType;
			logger.debug("onError(ui) for file info {}", fi);
			queue.remove(queueId);
			fireFailed(new FailedEvent(TusMultiUpload.this, fi, new Exception(errorReason)));
			hasUploadInProgress = false;
		}

		@Override
		public void onFileUploaded(String id, String name, String contentType) {
			/*
			FileInfo fi = new FileInfo();
			fi.id = id;
			fi.suggestedFilename = name;
			fi.suggestedFiletype = contentType;
			fireUploadSuccess(new SucceededEvent(TusMultiUpload.this, fi, null));
			*/
		}

		@Override
		public void onProgress(String queueId, String name, long uploadedBytes, long totalBytes) {
			FileInfo fi = new FileInfo();
			fi.queueId = queueId;
			fi.suggestedFilename = name;
			fi.offset = uploadedBytes;
			fi.entityLength = totalBytes;			
			logger.debug("onProgress(ui) for file info {}", fi);
			fireUpdateProgress(uploadedBytes, totalBytes);
			fireUpdateProgress(new ProgressEvent(TusMultiUpload.this, fi));				
		}

		@Override
		public void setNextQueuedFileIdAndStart(String queueId) {
			currentQueuedFileId = queueId;
			clientRpc.submitUpload();
		}

		@Override
		public void onFileCountError(int newlyAddedFiles) {
			Notification.show(MessageFormat.format(fileCountErrorMessagePattern, TusMultiUpload.this.getMaxFileCount(), TusMultiUpload.this.getRemainingQueueSeats() ,newlyAddedFiles), Type.ERROR_MESSAGE);
		}

		@Override
		public void onFileSizeError(JsonArray fileArray) {
			List<String >fileList = new ArrayList<>();
			for (int i=0; i < fileArray.length() ; i++) {
				JsonObject o = (JsonObject)fileArray.get(i);
				String filename = o.getString("filename");
				Long filesize = (long) o.getNumber("filesize");
				fileList.add(filename+" ("+readableFileSize(filesize)+")");
			}
			Notification.show(MessageFormat.format(fileSizeErrorMessagePattern, readableFileSize(TusMultiUpload.this.getMaxFileSize()), String.join(", ", fileList)), Type.ERROR_MESSAGE);			
		}

	  }
	  
	  /*********/
	  /**
	   * The stream variable that maps the stream events to the upload component and
	   * the configured data receiver.
	   */
	  private class StreamVariableImpl implements
	      com.vaadin.server.StreamVariable {
		protected static final long THROTTLE_EVENTS_MS = 1000L;

		long lastProgress = 0L;

		@Override
		public OutputStream getOutputStream() {
			// TODO add a receiver
			return null;
		}

		@Override
		public boolean listenProgress() {
			return !getState().clientSideProgress;
		}

		@Override
		public void streamingStarted(StreamingStartEvent event) {
			StreamingEvents.TusStreamingEvent tevt = (StreamingEvents.TusStreamingEvent) event;
			tevt.getFileInfo().queueId = currentQueuedFileId;
			lastProgress = 0;
			logger.debug("streamingStarted(StreamingStartEvent) for file info {}", tevt.getFileInfo());
            if (TusMultiUpload.this.getUI() != null && !TusMultiUpload.this.getUI().isClosing()) {
                TusMultiUpload.this.getUI().access(() -> fireStarted(new StartedEvent(TusMultiUpload.this, tevt.getFileInfo())) );
			} else {
				fireStarted(new StartedEvent(TusMultiUpload.this, tevt.getFileInfo()));
			}
			hasUploadInProgress = true;
		}
		
		@Override
		public void onProgress(StreamingProgressEvent event) {
			// throttle to speedup and avoid excessive session lock
			if (System.currentTimeMillis() >= lastProgress + THROTTLE_EVENTS_MS) {
				fireUpdateProgress(event.getBytesReceived(), event.getContentLength());
				StreamingEvents.TusStreamingEvent tevt = (StreamingEvents.TusStreamingEvent) event;
				tevt.getFileInfo().queueId = currentQueuedFileId;
				logger.debug("onProgress(StreamingProgressEvent) for file info {}", tevt.getFileInfo());
				if (TusMultiUpload.this.getUI() != null && !TusMultiUpload.this.getUI().isClosing()) {
					TusMultiUpload.this.getUI().access(() -> fireUpdateProgress(new ProgressEvent(TusMultiUpload.this, tevt.getFileInfo())));
				} else {
					fireUpdateProgress(new ProgressEvent(TusMultiUpload.this, tevt.getFileInfo()));
				}
				lastProgress = System.currentTimeMillis();
			}
		}

		@Override
		public void streamingFinished(StreamingEndEvent event) {
			StreamingEvents.TusStreamingEvent tevt = (StreamingEvents.TusStreamingEvent) event;
			Datastore dataStore = getReceiverDataStore();
			lastProgress = 0;
			if (dataStore != null) {
				try {
					// uses TUS calculated uuid;
					String id = tevt.getFileInfo().id;
					tevt.getFileInfo().queueId = currentQueuedFileId;
					logger.debug("streamingFinished(StreamingEndEvent) for file info {}", tevt.getFileInfo());
					//InputStream is = dataStore.getInputStream(id);
					URI path = dataStore.getInputStreamPath(id).toUri();
					tevt.getFileInfo().setUploadURL(path);
					queue.remove(currentQueuedFileId);
					queueBatch.add(tevt.getFileInfo());
					if (TusMultiUpload.this.getUI() != null && !TusMultiUpload.this.getUI().isClosing()) {
                        TusMultiUpload.this.getUI().access(() -> {
							fireUploadSuccess(new SucceededEvent(TusMultiUpload.this, tevt.getFileInfo(), queue.size()));
							try {
								dataStore.terminate(id);
							} catch (Exception e) {
								logger.warn("dataStore terminate pb for file info {}", tevt.getFileInfo(), e);
							}
						});
					} else {
						fireUploadSuccess(new SucceededEvent(TusMultiUpload.this, tevt.getFileInfo(), queue.size()));
						dataStore.terminate(id);
					}

					if(queue.isEmpty()) {
						if (TusMultiUpload.this.getUI() != null && !TusMultiUpload.this.getUI().isClosing()) {
							TusMultiUpload.this.getUI().access(() -> {

								fireUploadComplete(new Events.CompleteEvent(TusMultiUpload.this, new HashSet<>(queueBatch), new HashSet<>(queueFailedBatch)));
								queueBatch.clear();
								queueFailedBatch.clear();
								try {
									dataStore.terminate(id);
								} catch (Exception e) {
									logger.warn("dataStore terminate pb for file info {}", tevt.getFileInfo(), e);
								}
							});
						} else {
							fireUploadComplete(new Events.CompleteEvent(TusMultiUpload.this, new HashSet<>(queueBatch), new HashSet<>(queueFailedBatch)));
							queueBatch.clear();
							queueFailedBatch.clear();
							dataStore.terminate(id);
						}
					}
				} catch (Exception e) {
					logger.warn("streamingFinished pb for file info {}", tevt.getFileInfo(), e);
				}
			}
			hasUploadInProgress = false;
		}

		@Override
		public void streamingFailed(StreamingErrorEvent event) {
			StreamingEvents.TusStreamingEvent tevt = (StreamingEvents.TusStreamingEvent) event;
			tevt.getFileInfo().queueId = currentQueuedFileId;
			lastProgress = 0;
			queue.remove(currentQueuedFileId);
			queueFailedBatch.add(tevt.getFileInfo());
			logger.debug("streamingFailed(StreamingErrorEvent) for file info {}", tevt.getFileInfo());
			if (TusMultiUpload.this.getUI() != null && !TusMultiUpload.this.getUI().isClosing()) {
                TusMultiUpload.this.getUI().access(() -> fireFailed(new FailedEvent(TusMultiUpload.this, tevt.getFileInfo(), event.getException())) );
			} else {
				fireFailed(new FailedEvent(TusMultiUpload.this, tevt.getFileInfo(), event.getException()));	
			}
			
			hasUploadInProgress = false;
		}

		@Override
		public boolean isInterrupted() {
			return false;
		}

	  }


}
