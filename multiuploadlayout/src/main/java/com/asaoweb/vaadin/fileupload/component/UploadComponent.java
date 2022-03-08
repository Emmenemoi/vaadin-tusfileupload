package com.asaoweb.vaadin.fileupload.component;

import com.asaoweb.vaadin.fileupload.events.Events;
import com.vaadin.shared.Registration;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class UploadComponent extends AbstractJavaScriptComponent {
    private final static Method SUCCEEDED_METHOD;
    private final static Method STARTED_METHOD;
    private final static Method QUEUED_METHOD;
    private final static Method PROGRESS_METHOD;
    private final static Method FINISHED_METHOD;
    private final static Method FAILED_METHOD;

    static {
        try {
            SUCCEEDED_METHOD = Events.SucceededListener.class.getMethod(
                    "uploadSucceeded", Events.SucceededEvent.class);
            FAILED_METHOD = Events.FailedListener.class.getMethod(
                    "uploadFailed", Events.FailedEvent.class);
            STARTED_METHOD = Events.StartedListener.class.getMethod(
                    "uploadStarted", Events.StartedEvent.class);
            QUEUED_METHOD = Events.FileQueuedListener.class.getMethod(
                    "uploadFileQueued", Events.FileQueuedEvent.class);
            PROGRESS_METHOD = Events.ProgressListener.class.getMethod(
                    "uploadProgress", Events.ProgressEvent.class);
            FINISHED_METHOD = Events.FinishedListener.class.getMethod(
                    "uploadFinished", Events.FinishedEvent.class);
        }
        catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException("Unable to find listener event method.", ex);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(UploadComponent.class.getName());

    /**
     * The list of native progress listeners to be notified during the upload.
     */
    protected final List<Upload.ProgressListener> progressListeners = new ArrayList<>();


    protected Set<String> queue = Collections.synchronizedSet(new HashSet<>());

    protected String fileCountErrorMessagePattern = "Too many files uploaded: total {0,,max} files max and {1,,current} uploaded or queued but tried to add {2,,tried} more!";
    protected String fileSizeErrorMessagePattern = "Some files are too big (limit is {0,,limitStr}): {1,,fileListString}";

    protected boolean hasUploadInProgress = false;
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
    protected void fireFailed(Events.FailedEvent evt) {
        fireEvent(evt);
    }

    /**
     * Fires the upload success event to all registered listeners.
     *
     * @param evt the event details
     */
    protected void fireUploadSuccess(Events.SucceededEvent evt) {
        LoggerFactory.getLogger(getClass()).debug("fireUploadSuccess {}", evt);
        if(preprocessOnSuccess(evt)) {
                    LoggerFactory.getLogger(getClass()).debug("before fireEvent {}", evt);
                    fireEvent(evt);
        } else {
            LoggerFactory.getLogger(getClass()).debug("ignore fireEvent {}", evt);
        }
    }

    /**
     * Fires the upload queued event to all registered listeners.
     *
     * @param evt the event details
     */
    protected void fireQueued(Events.FileQueuedEvent evt) {
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
    protected void fireUpdateProgress(Events.ProgressEvent evt) {
        fireEvent(evt);
    }

    /**
     * Adds the given listener for upload failed events.
     *
     * @param listener the listener to add
     */
    public Registration addFailedListener(Events.FailedListener listener) {
        return addListener(Events.FailedEvent.class, listener, FAILED_METHOD);
    }

    /**
     * Adds the given listener for upload finished events.
     *
     * @param listener the listener to add
     */
    public Registration addFinishedListener(Events.FinishedListener listener) {
        return addListener(Events.FinishedEvent.class, listener, FINISHED_METHOD);
    }

    /**
     * Adds the given listener for upload queued events.
     *
     * @param listener the listener to add
     */
    public Registration addFileQueuedListener(Events.FileQueuedListener listener) {
        return addListener(Events.FileQueuedEvent.class, listener, QUEUED_METHOD);
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
    public Registration addProgressListener(Events.ProgressListener listener) {
        return addListener(Events.ProgressEvent.class, listener, PROGRESS_METHOD);
    }

    /**
     * Adds the given listener for upload started events.
     *
     * @param listener the listener to add
     */
    public Registration addStartedListener(Events.StartedListener listener) {
        return addListener(Events.StartedEvent.class, listener, STARTED_METHOD);
    }

    /**
     * Adds the given listener for upload succeeded events.
     *
     * @param listener the listener to add
     */
    public Registration addSucceededListener(Events.SucceededListener listener) {
        return addListener(Events.SucceededEvent.class, listener, SUCCEEDED_METHOD);
    }

    /**
     * Fires the upload started event to all registered listeners.
     *
     * @param evt the started event to fire
     */
    protected void fireStarted(Events.StartedEvent evt) {
        fireEvent(evt);
    }

    protected boolean preprocessOnSuccess(Events.SucceededEvent evt) {
        return true;
    }

    public abstract void removeFromQueue(String fileId); /*{
        if (queueId != null) {
            if (queueId.equals(currentQueuedFileId)) {
                clientRpc.abortUpload();
            } else {
                clientRpc.removeFromQueue(queueId);
            }
            queue.remove(queueId);
        }
    }*/

    public abstract void setRemainingQueueSeats(int remainingQueueSeats); /* {
        remainingQueueSeats = Math.max(0,  remainingQueueSeats);
        if (remainingQueueSeats <= getMaxFileCount()) {
            getState().remainingQueueSeats = remainingQueueSeats;
        }
    }*/

    public abstract Long getMaxFileSize(); /*{
        return getState().maxFileSize;
    }*/

    public abstract Integer getMaxFileCount(); /*{
        return getState().maxFileCount;
    }*/

    public abstract void setMultiple(boolean multiple);

    public abstract boolean isMultiple();

    public int getQueueCount() {
        return queue.size();
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public boolean hasUploadInProgress() {
        return hasUploadInProgress;
    }

    public abstract void abortAll();

    public abstract void setChunkSize(long chunkSize);

    public abstract void setWithCredentials(boolean withCredentials);

    public abstract void setMaxFileCount(int maxFileCount);

    public abstract void setMaxFileSize(long maxFileSize);

    public void setButtonCaption(String caption) {
    }

    public abstract void setClientSideDebug(boolean debugMode);

    public abstract void setAcceptFilter(Collection<String> filters);

    /**
     * Sets the caption displayed on the error notification for bad file count.
     * Default: "Too many files uploaded: total {0,,max} files max and {1,,current} uploaded or queued but tried to add {2,,tried} more!";
     *
     * @param fileCountErrorMessagePattern String pattern
     */
    public void setFileCountErrorMessagePattern(String fileCountErrorMessagePattern) {
        this.fileCountErrorMessagePattern = fileCountErrorMessagePattern;
    }

    /**
     * Sets the caption displayed on the error notification for bad file count.
     * Default: "Some files are too big (limit is {0,,limitStr}): {1,,fileListString}";
     *
     * @param fileSizeErrorMessagePattern String pattern
     */
    public void setFileSizeErrorMessagePattern(String fileSizeErrorMessagePattern) {
        this.fileSizeErrorMessagePattern = fileSizeErrorMessagePattern;
    }
}
