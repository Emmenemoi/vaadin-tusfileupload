package com.asaoweb.vaadin.tusfileupload.events;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.vaadin.server.StreamVariable.StreamingEndEvent;
import com.vaadin.server.StreamVariable.StreamingEvent;
import com.vaadin.server.StreamVariable.StreamingProgressEvent;
import com.vaadin.server.StreamVariable.StreamingStartEvent;

public class StreamingEvents {
	public interface TusStreamingEvent extends StreamingEvent {
		 public FileInfo getFileInfo();
	}
	
	static abstract class AbstractStreamingEvent implements TusStreamingEvent {
	    private final FileInfo fileInfo;

	    protected AbstractStreamingEvent(FileInfo fileInfo) {
	    	this.fileInfo = fileInfo;
	    }

	    @Override
	    public final String getFileName() {
	        return fileInfo.suggestedFilename;
	    }

	    @Override
	    public final String getMimeType() {
	        return fileInfo.suggestedFiletype;
	    }
	    
	    @Override
	    public final long getContentLength() {
	        return fileInfo.entityLength;
	    }

	    @Override
	    public final long getBytesReceived() {
	        return fileInfo.offset;
	    }
	    
	    @Override
	    public final FileInfo getFileInfo() {
	    	return this.fileInfo;
	    }

	}
	
	@SuppressWarnings("serial")
	public static final class StreamingStartEventImpl extends AbstractStreamingEvent
	        implements StreamingStartEvent {

	    private boolean disposed;

	    public StreamingStartEventImpl(FileInfo fi) {
	        super(fi);
	    }

	    @Override
	    public void disposeStreamVariable() {
	        disposed = true;
	    }

	    boolean isDisposed() {
	        return disposed;
	    }

	}
	
	@SuppressWarnings("serial")
	public static final class StreamingEndEventImpl extends AbstractStreamingEvent
	        implements StreamingEndEvent {

	    public StreamingEndEventImpl(FileInfo fi) {
	        super(fi);
	    }
	}
	
	@SuppressWarnings("serial")
	public static final class StreamingProgressEventImpl extends AbstractStreamingEvent
	        implements StreamingProgressEvent {

	    public StreamingProgressEventImpl(FileInfo fi) {
	        super(fi);
	    }
	}
}
