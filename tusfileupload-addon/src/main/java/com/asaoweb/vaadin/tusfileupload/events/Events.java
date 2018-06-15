package com.asaoweb.vaadin.tusfileupload.events;

import java.io.FileInputStream;
import java.io.InputStream;

import com.asaoweb.vaadin.tusfileupload.FileInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.Upload;

public class Events {
	public static abstract class AbstractTusUploadEvent extends Component.Event {
	    private final FileInfo fileInfo;

	    protected AbstractTusUploadEvent(Component source, FileInfo fileInfo) {
	    	super(source);
	        this.fileInfo = fileInfo;
	    }

	    /**
	     * Returns the file name.
	     *
	     * @return the file name
	     */
	    public String getFilename() {
	      return fileInfo.suggestedFilename;
	    }

	    /**
	     * Returns the mime-type.
	     *
	     * @return the mime-type
	     */
	    public String getMimeType() {
	      return fileInfo.suggestedFiletype;
	    }

	    /**
	     * Returns the content length in bytes.
	     *
	     * @return the length in bytes
	     */
	    public long getLength() {
	      return fileInfo.entityLength;
	    }
	    
	    /**
	     * Returns the id.
	     *
	     * @return the id
	     */
	    public String getId() {
	      return fileInfo.id;
	    }
	    
	    /**
	     * Returns the queue id.
	     *
	     * @return the queue id
	     */
	    public String getQueueId() {
	      return fileInfo.queueId;
	    }
	    
	    /**
	     * Returns the full FileInfo object.
	     *
	     * @return the FileInfo
	     */
	    public FileInfo getFileInfo() {
	      return fileInfo;
	    }
	}
	
	  /**
	   * The event fired when an upload completes, both success or failure.
	   */
	  public static class FinishedEvent extends AbstractTusUploadEvent {

	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the name of the file provided by the client
	     * @param mimeType the mime-type provided by the client
	     * @param length the content length in bytes provided by the client
	     */
	    public FinishedEvent(Component source, FileInfo fileInfo) {
	      super(source, fileInfo);
	    }

	  }
	  
	  /**
	   * A listener for finished events.
	   */
	  public interface FinishedListener {

	    /**
	     * Called when an upload finishes, either success or failure.
	     *
	     * @param evt the event describing the completion
	     */
	    void uploadFinished(FinishedEvent evt);
	  }

	  /**
	   * An event describing an upload failure.
	   */
	  public static class FailedEvent extends AbstractTusUploadEvent {

	    private final Exception reason;

	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the name of the file provided by the client
	     * @param mimeType the mime-type provided by the client
	     * @param length the content length in bytes provided by the client
	     * @param reason the root cause exception
	     */
	    public FailedEvent(Component source, FileInfo fileInfo, Exception reason) {
	      super(source, fileInfo);
	      this.reason = reason;
	    }

	    /**
	     * Returns the root cause exception if available.
	     *
	     * @return the root exception
	     */
	    public Exception getReason() {
	      return reason;
	    }
	  }

	  /**
	   * A listener for failed events.
	   */
	  public interface FailedListener {

	    /**
	     * Called when an upload fails.
	     *
	     * @param evt the event details
	     */
	    void uploadFailed(FailedEvent evt);
	  }

	  /**
	   * An event describing the start of an upload.
	   */
	  public static class StartedEvent extends AbstractTusUploadEvent {

	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the name of the file provided by the client
	     * @param mimeType the mime-type provided by the client
	     * @param contentLength the content length in bytes provided by the client
	     */
	    public StartedEvent(Component source, FileInfo fileInfo) {
		      super(source, fileInfo);
		    }
	  }

	  /**
	   * A listener that receives started events.
	   */
	  public interface StartedListener {

	    /**
	     * Called when the upload is started.
	     *
	     * @param evt the event details
	     */
	    void uploadStarted(StartedEvent evt);
	  }
	  
	  /**
	   * An event describing the start of an upload.
	   */
	  public static class ProgressEvent extends AbstractTusUploadEvent {

	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the name of the file provided by the client
	     * @param mimeType the mime-type provided by the client
	     * @param contentLength the content length in bytes provided by the client
	     */
	    public ProgressEvent(Component source, FileInfo fileInfo) {
	    	super(source, fileInfo);
		}
	    
	    public float getProgressPct() {
	    	return (float)this.getFileInfo().offset / (float)this.getFileInfo().entityLength;
	    }
	  }

	  /**
	   * A listener that receives started events.
	   */
	  public interface ProgressListener {

	    /**
	     * Called when the upload is started.
	     *
	     * @param evt the event details
	     */
	    void uploadProgress(ProgressEvent evt);
	  }

	  /**
	   * An event describing a successful upload.
	   */
	  public static class SucceededEvent extends FinishedEvent {

		 final InputStream inputStream;
		  
	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the name of the file provided by the client
	     * @param mimeType the mime-type provided by the client
	     * @param length the content length in bytes provided by the client
	     */
	    public SucceededEvent(Component source, FileInfo fileInfo, InputStream inputStream) {
		      super(source, fileInfo);
		      this.inputStream = inputStream;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

	  }

	  /**
	   * A listener that receives upload success events.
	   */
	  public interface SucceededListener {

	    /**
	     * Called when an upload is successful.
	     *
	     * @param evt the event details
	     */
	    void uploadSucceeded(SucceededEvent evt);
	  }

	  /**
	   * An event describing a queued upload.
	   */
	  public static class FileQueuedEvent extends AbstractTusUploadEvent {
		  
	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the FileIPnfo of the file provided by the client
	     */
	    public FileQueuedEvent(Component source, FileInfo fileInfo) {
		      super(source, fileInfo);
		 }

	  }

	  /**
	   * A listener that receives upload queued events.
	   */
	  public interface FileQueuedListener {

	    /**
	     * Called when is added to the queue.
	     *
	     * @param evt the event details
	     */
	    void uploadFileQueued(FileQueuedEvent evt);
	  }
	  
	  /**
	   * An event describing a deleted upload.
	   */
	  public static class FileDeletedClickEvent extends AbstractTusUploadEvent {
		  
	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the FileInfo of the file provided by the client
	     */
	    public FileDeletedClickEvent(Component source, FileInfo fileInfo) {
		      super(source, fileInfo);
		 }

	  }

	  /**
	   * A listener that receives file deleted click events.
	   */
	  public interface FileDeletedClickListener {

	    /**
	     * Called when is added to the queue.
	     *
	     * @param evt the event details
	     */
	    void fileDeletedClick(FileDeletedClickEvent evt);
	  }
	  
	  /**
	   * An event describing a deleted upload.
	   */
	  public static class FileIndexMovedEvent extends AbstractTusUploadEvent {
		protected final int newIndex;
		protected final int oldIndex;
		
	    /**
	     * Constructs the event.
	     *
	     * @param source the source component
	     * @param filename the FileInfo of the file provided by the client
	     */
	    public FileIndexMovedEvent(Component source, FileInfo fileInfo, int oldIndex, int newIndex) {
		      super(source, fileInfo);
		      this.oldIndex = oldIndex;
		      this.newIndex = newIndex;
		 }

		public int getNewIndex() {
			return newIndex;
		}

		public int getOldIndex() {
			return oldIndex;
		}

		@Override
		public String toString() {
			return "FileIndexMovedEvent [newIndex=" + newIndex + ", oldIndex=" + oldIndex + "]";
		}

	  }

	  /**
	   * A listener that receives file deleted click events.
	   */
	  public interface FileIndexMovedListener {

	    /**
	     * Called when is added to the queue.
	     *
	     * @param evt the event details
	     */
	    void fileIndexMoved(FileIndexMovedEvent evt);
	  }
}
