package com.asaoweb.vaadin.tusfileupload.component;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.tusfileupload.Composer;
import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.FileInfo;
import com.asaoweb.vaadin.tusfileupload.TUSFileUploadHandler;
import com.asaoweb.vaadin.tusfileupload.data.Datastore;
import com.asaoweb.vaadin.tusfileupload.events.Events.FailedEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.FailedListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.FileQueuedEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.FileQueuedListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.FinishedEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.FinishedListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.ProgressEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.ProgressListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.StartedEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.StartedListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.SucceededEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.SucceededListener;
import com.asaoweb.vaadin.tusfileupload.events.StreamingEvents.TusStreamingEvent;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException.ConfigError;
import com.asaoweb.vaadin.tusfileupload.shared.TusMultiuploadClientRpc;
import com.asaoweb.vaadin.tusfileupload.shared.TusMultiuploadServerRpc;
import com.asaoweb.vaadin.tusfileupload.shared.TusMultiuploadState;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServletPortletHelper;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.Registration;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Upload;

import elemental.json.JsonArray;
import elemental.json.JsonObject;


@SuppressWarnings("serial")
@JavaScript(value = {"//rawgit.com/Emmenemoi/tus-js-client/master/dist/tus.js", "vaadin://addons/tusfileupload/tusmultiupload-connector.js"})
public class TusMultiUpload extends AbstractJavaScriptComponent {
	  private final static Method SUCCEEDED_METHOD;
	  private final static Method STARTED_METHOD;
	  private final static Method QUEUED_METHOD;
	  private final static Method PROGRESS_METHOD;
	  private final static Method FINISHED_METHOD;
	  private final static Method FAILED_METHOD;

	  static {
	    try {
	      SUCCEEDED_METHOD = SucceededListener.class.getMethod(
	          "uploadSucceeded", SucceededEvent.class);
	      FAILED_METHOD = FailedListener.class.getMethod(
	    	  "uploadFailed", FailedEvent.class);
	      STARTED_METHOD = StartedListener.class.getMethod(
	          "uploadStarted", StartedEvent.class);
	      QUEUED_METHOD = FileQueuedListener.class.getMethod(
		      "uploadFileQueued", FileQueuedEvent.class);
	      PROGRESS_METHOD = ProgressListener.class.getMethod(
		      "uploadProgress", ProgressEvent.class);
	      FINISHED_METHOD = FinishedListener.class.getMethod(
	          "uploadFinished", FinishedEvent.class);
	    }
	    catch (NoSuchMethodException | SecurityException ex) {
	      throw new RuntimeException("Unable to find listener event method.", ex);
	    }
	  }
	  
	  private static final Logger logger = LoggerFactory.getLogger(TusMultiUpload.class.getName());

	 /**
	   * The list of native progress listeners to be notified during the upload.
	   */
	  protected final List<Upload.ProgressListener> progressListeners = new ArrayList<>();

	  /**
	   * The receiver registered with the upload component that all data will be
	   * streamed into.
	   */
	  protected Datastore 	receiverDataStore;
	  protected Config		receiverConfig;
	  
	  private final TusMultiuploadServerRpc serverRpc = new ServerRpcImpl();
	  private final TusMultiuploadClientRpc clientRpc;

	  private StreamVariable streamVariable;
	  private String currentQueuedFileId = "";
	  private boolean hasUploadInProgress = false;

	  protected Set<String> queue = Collections.synchronizedSet(new HashSet<>());
	  
	  protected String fileCountErrorMessagePattern = "Too many files uploaded: total {0,,max} files max and {1,,current} uploaded or queued but tried to add {2,,tried} more!";
	  protected String fileSizeErrorMessagePattern = "Some files are too big (limit is {0,,limitStr}): {1,,fileListString}";

	  /**
	   * Constructs the upload component.
	   * @throws ConfigError 
	   */
	  public TusMultiUpload() throws ConfigError {
	    this(null, null);
	  }

	  /**
	   * Constructs the upload component.
	   *
	   * @param caption the caption of the component
	   * @param receiver the receiver to create the output stream to receive upload
	   * data
	   * @throws ConfigError 
	   */
	  public TusMultiUpload(String buttonCaption, Config receiverConfig) throws ConfigError {
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
		} catch (ConfigError e) {
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
	   * @throws ConfigError 
	   */
	  protected void installHandler() throws ConfigError {
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
	   * Returns true if the component is enabled. This implementation always
	   * returns true even if the component is set to disabled. This is required
	   * because we want the ability to disable the browse/submit buttons while
	   * still allowing an upload in progress to continue. The implementation relies
	   * on RPC calls so the overall component must always be enabled or the upload
	   * complete RPC call will be dropped.
	   *
	   * @return always true
	   */
	  @Override
	  public boolean isConnectorEnabled() {
	    return true;
	  }

	  /**
	   * Fires the upload failed event to all registered listeners.
	   *
	   * @param evt the event details
	   */
	  protected void fireFailed(FailedEvent evt) {
	    fireEvent(evt);
	  }
	  
	  /**
	   * Fires the upload success event to all registered listeners.
	   *
	   * @param evt the event details
	   */
	  protected void fireUploadSuccess(SucceededEvent evt) {
	    fireEvent(evt);
	  }

	  /**
	   * Fires the upload queued event to all registered listeners.
	   *
	   * @param evt the event details
	   */
	  protected void fireQueued(FileQueuedEvent evt) {
	    fireEvent(evt);
	  }
	  
	  /**
	   * Fires the legacy progress event to all registered listeners.
	   *
	   * @param totalBytes bytes received so far
	   * @param contentLength actual size of the file being uploaded, if known
	   *
	   */
	  protected void fireUpdateProgress(long totalBytes, long contentLength) {
	    // This is implemented differently than other listeners to maintain
	    // backwards compatibility
	    if (progressListeners != null) {
	      for (Upload.ProgressListener l : progressListeners) {
	        l.updateProgress(totalBytes, contentLength);
	      }
	    }
	  }
	  
	  /**
	   * Fires the upload progress event to all registered listeners.
	   *
	   * @param evt the event details
	   */
	  protected void fireUpdateProgress(ProgressEvent evt) {
	    fireEvent(evt);
	  }
	  
	  /**
	   * Adds the given listener for upload failed events.
	   *
	   * @param listener the listener to add
	   */
	  public Registration addFailedListener(FailedListener listener) {
	    return addListener(FailedEvent.class, listener, FAILED_METHOD);
	  }
	  
	  /**
	   * Adds the given listener for upload finished events.
	   *
	   * @param listener the listener to add
	   */
	  public Registration addFinishedListener(FinishedListener listener) {
	    return addListener(FinishedEvent.class, listener, FINISHED_METHOD);
	  }
	  
	  /**
	   * Adds the given listener for upload queued events.
	   *
	   * @param listener the listener to add
	   */
	  public Registration addFileQueuedListener(FileQueuedListener listener) {
	    return addListener(FileQueuedEvent.class, listener, QUEUED_METHOD);
	  }
	  

	  /**
	   * Adds the given legacy listener for native upload progress events.
	   *
	   * @param listener the listener to add
	   */
	  public void addProgressListener(Upload.ProgressListener listener) {
	    progressListeners.add(listener);
	  }

	  /**
	   * Adds the given listener for upload progress events.
	   *
	   * @param listener the listener to add
	   */
	  public Registration addProgressListener(ProgressListener listener) {
	    return addListener(ProgressEvent.class, listener, PROGRESS_METHOD);
	  }
	  
	  /**
	   * Adds the given listener for upload started events.
	   *
	   * @param listener the listener to add
	   */
	  public Registration addStartedListener(StartedListener listener) {
	    return addListener(StartedEvent.class, listener, STARTED_METHOD);
	  }

	  /**
	   * Adds the given listener for upload succeeded events.
	   *
	   * @param listener the listener to add
	   */
	  public Registration addSucceededListener(SucceededListener listener) {
	    return addListener(SucceededEvent.class, listener, SUCCEEDED_METHOD);
	  }
	  
	  /**
	   * Fires the upload started event to all registered listeners.
	   *
	   * @param evt the started event to fire
	   */
	  protected void fireStarted(StartedEvent evt) {
	    fireEvent(evt);
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
	   * @param receiver the receiver configuration to use for creating file output streams
	 * @throws Exception 
	   */
	  public void setReceiverDatastore(Config receiverConfig) throws ConfigError {
		  this.receiverConfig = receiverConfig;
		  Composer c;
			try {
				c = new Composer(receiverConfig);
			} catch (Exception e) {
				throw new ConfigError(e.getMessage());
			}
			setReceiverDatastore(c.getDatastore());
	  }
	  
	  private void setReceiverDatastore(Datastore receiverDataStore) {
		  this.receiverDataStore = receiverDataStore;
	  }
	  
	  @Override
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
	   * Sets the caption displayed on the error notification for bad file count.
	   * Default: "Too many files uploaded: total {0,,max} files max and {1,,current} uploaded or queued but tried to add {2,,tried} more!";
	   * 
	   * @param String pattern
	   */
	  public void setFileCountErrorMessagePattern(String fileCountErrorMessagePattern) {
		  this.fileCountErrorMessagePattern = fileCountErrorMessagePattern;
	 }
	  
	  /**
	   * Sets the caption displayed on the error notification for bad file count.
	   * Default: "Some files are too big (limit is {0,,limitStr}): {1,,fileListString}";
	   * 
	   * @param String pattern
	   */
	  public void setFileSizeErrorMessagePattern(String fileSizeErrorMessagePattern) {
		  this.fileSizeErrorMessagePattern = fileSizeErrorMessagePattern;
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
	  public void setButtonCaption(String caption) {
	    getState().buttonCaption = caption;
	    getState().rebuild = true;
	  }
	  
	  /**
	   * Activates and receive the events on client side progress
	   *
	   * @param should manage client side progress
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
	   * @param set chunk size in bytes (0 for no limit)
	   */
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
	   * @param boolean allowing passing http credentials in requests
	   */
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
	   * @param set the retry delays as an array of int or null
	   */
	  public void setRetryDelays(int[] retryDelays) {
		  getState().retryDelays = retryDelays;
	  }
	  
	  public int[] getRetryDelays() {
		  return getState().retryDelays;
	  }
	  
	  public void setAcceptFilter(String filter) {
	    getState().mimeAccept = filter;
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
	  
	  public void setMaxFileSize(long maxFileSize) {
		  getState().maxFileSize = maxFileSize;
	  }
	  
	  public long getMaxFileSize() {
		  return getState().maxFileSize;
	  }
	  
	  public void setMaxFileCount(int maxFileCount) {
		  if (maxFileCount == 1) {
			  this.setMultiple(false);
		  } else {
			  this.setMultiple(true);			  
		  }
		  getState().maxFileCount = maxFileCount;	  
		  getState().remainingQueueSeats = maxFileCount;
	  }
	  
	  public int getMaxFileCount() {
		  return getState().maxFileCount;
	  }
	  
	  public void setRemainingQueueSeats(int remainingQueueSeats) {
		  remainingQueueSeats = Math.max(0,  remainingQueueSeats);
		  if (remainingQueueSeats <= getMaxFileCount()) {
			  getState().remainingQueueSeats = remainingQueueSeats;	
		  }
	  }
	  
	  public int getRemainingQueueSeats() {
		  return getState().remainingQueueSeats;
	  }
	  
	  public boolean hasUploadInProgress() {
		  return hasUploadInProgress;
	  }

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
			TusStreamingEvent tevt = (TusStreamingEvent) event;
			tevt.getFileInfo().queueId = currentQueuedFileId;
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
			fireUpdateProgress(event.getBytesReceived(), event.getContentLength());
			TusStreamingEvent tevt = (TusStreamingEvent) event;
			tevt.getFileInfo().queueId = currentQueuedFileId;
			logger.debug("onProgress(StreamingProgressEvent) for file info {}", tevt.getFileInfo());
			if (TusMultiUpload.this.getUI() != null && !TusMultiUpload.this.getUI().isClosing()) {
                TusMultiUpload.this.getUI().access(() -> fireUpdateProgress(new ProgressEvent(TusMultiUpload.this, tevt.getFileInfo())) );
			} else {
				fireUpdateProgress(new ProgressEvent(TusMultiUpload.this, tevt.getFileInfo()));	
			}
		}

		@Override
		public void streamingFinished(StreamingEndEvent event) {
			TusStreamingEvent tevt = (TusStreamingEvent) event;
			Datastore dataStore = getReceiverDataStore();
			if (dataStore != null) {
				try {
					// uses TUS calculated uuid;
					String id = tevt.getFileInfo().id;
					tevt.getFileInfo().queueId = currentQueuedFileId;
					logger.debug("streamingFinished(StreamingEndEvent) for file info {}", tevt.getFileInfo());
					InputStream is = dataStore.getInputStream(id);
					queue.remove(currentQueuedFileId);
					if (TusMultiUpload.this.getUI() != null && !TusMultiUpload.this.getUI().isClosing()) {
                        TusMultiUpload.this.getUI().access(() -> fireUploadSuccess(new SucceededEvent(TusMultiUpload.this, tevt.getFileInfo(), is)) );
					} else {
						fireUploadSuccess(new SucceededEvent(TusMultiUpload.this, tevt.getFileInfo(), is));
					}
					
					dataStore.terminate(id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			hasUploadInProgress = false;
		}

		@Override
		public void streamingFailed(StreamingErrorEvent event) {
			TusStreamingEvent tevt = (TusStreamingEvent) event;
			tevt.getFileInfo().queueId = currentQueuedFileId;
			queue.remove(currentQueuedFileId);
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

	  public static String readableFileSize(long size) {
		    if(size <= 0) return "0";
		    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
		}
}
