package com.asaoweb.vaadin.fileupload.events;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;

import com.asaoweb.vaadin.fileupload.component.UploadComponent;
import com.asaoweb.vaadin.fileupload.FileInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

public class Events {
    public static abstract class AbstractTusUploadEvent extends Component.Event {
        private final FileInfo fileInfo;
        private final UI ui;

        protected AbstractTusUploadEvent(UploadComponent source, FileInfo fileInfo) {
            super(source);
            this.fileInfo = fileInfo;
            this.ui = source != null ? source.getUI() : null;
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

        public UI getUI() { return ui;}

        @Override
        public UploadComponent getComponent() {
            return (UploadComponent) super.getComponent();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "fileInfo=" + fileInfo +
                    ", source=" + source +
                    '}';
        }

    }

    /**
     * The event fired when an upload completes, both success or failure.
     */
    public static class FinishedEvent extends AbstractTusUploadEvent {

        /**
         * Constructs the event.
         *
         * @param source   the source component
         * @param fileInfo File information provided by the client
         */
        public FinishedEvent(UploadComponent source, FileInfo fileInfo) {
            super(source, fileInfo);
        }

    }

    /**
     * A listener for finished events.
     */
    public interface FinishedListener extends Serializable {

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
         * @param source   the source component
         * @param fileInfo File information provided by the client
         * @param reason   the root cause exception
         */
        public FailedEvent(UploadComponent source, FileInfo fileInfo, Exception reason) {
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
    public interface FailedListener extends Serializable {

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
         * @param source        the source component
         * @param fileInfo File information provided by the client
         */
        public StartedEvent(UploadComponent source, FileInfo fileInfo) {
            super(source, fileInfo);
        }
    }

    /**
     * A listener that receives started events.
     */
    public interface StartedListener extends Serializable {

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
         * @param source        the source component
         * @param fileInfo File information provided by the client
         */
        public ProgressEvent(UploadComponent source, FileInfo fileInfo) {
            super(source, fileInfo);
        }

        public float getProgressPct() {
            return (float) this.getFileInfo().offset / (float) this.getFileInfo().entityLength;
        }
    }

    /**
     * A listener that receives started events.
     */
    public interface ProgressListener extends Serializable {

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
        URI inputStreamPath;
        //final int remainingQueueSize;
        boolean addFileToList = true;
        FileInfo finalFileInfo;

        /**
         * Constructs the event.
         *
         * @param source   the source component
         * @param fileInfo File information provided by the client
         */
        public SucceededEvent(UploadComponent source, FileInfo fileInfo, InputStream inputStream, URI inputStreamPath) {
            super(source, fileInfo);
            this.inputStream = inputStream;
            this.inputStreamPath = inputStreamPath;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public URI getInputStreamPath() { return inputStreamPath; }

        public void setInputStreamPath(URI inputStreamPath) {
            this.inputStreamPath = inputStreamPath;
        }

        //public int getRemainingQueueSize() { return remainingQueueSize; }

        public boolean shouldAddFileToList() {
            return addFileToList;
        }

        public void setAddFileToList(boolean addFileToList) {
            this.addFileToList = addFileToList;
        }

        public FileInfo getFinalFileInfo() {
            return finalFileInfo;
        }

        public void setFinalFileInfo(FileInfo finalFileInfo) {
            this.finalFileInfo = finalFileInfo;
        }

        @Override
        public String toString() {
            return "SucceededEvent{" +
                    "inputStreamPath=" + inputStreamPath +
                    "} " + super.toString();
        }
    }

    /**
     * A listener that receives upload success events.
     */
    public interface SucceededListener extends Serializable {

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
         * @param source   the source component
         * @param fileInfo File information provided by the client
         */
        public FileQueuedEvent(UploadComponent source, FileInfo fileInfo) {
            super(source, fileInfo);
        }

    }

    /**
     * A listener that receives upload queued events.
     */
    public interface FileQueuedListener extends Serializable {

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
         * @param source   the source component
         * @param fileInfo File information provided by the client
         */
        public FileDeletedClickEvent(UploadComponent source, FileInfo fileInfo) {
            super(source, fileInfo);
        }

    }

    /**
     * An event describing a deleted upload.
     */
    public static class InternalDeleteClickEvent extends AbstractTusUploadEvent {

        /**
         * Constructs the event.
         *
         * @param fileInfo File information provided by the client
         */
        public InternalDeleteClickEvent(UploadComponent source, FileInfo fileInfo) {
            super(source, fileInfo);
        }

    }

    /**
     * A listener that receives file deleted click events.
     */
    public interface FileDeletedClickListener extends Serializable  {

        /**
         * Called when is added to the queue.
         *
         * @param evt the event details
         */
        void fileDeletedClick(FileDeletedClickEvent evt);
    }

    /**
     * A listener that receives file deleted click events.
     */
    public interface InternalDeleteClickListener extends Serializable  {

        /**
         * Called when is added to the queue.
         *
         * @param evt the event details
         */
        void internalDeleteClick(InternalDeleteClickEvent evt);
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
         * @param source   the source component
         * @param fileInfo File information provided by the client
         */
        public FileIndexMovedEvent(UploadComponent source, FileInfo fileInfo, int oldIndex, int newIndex) {
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
    public interface FileIndexMovedListener extends Serializable {

        /**
         * Called when is added to the queue.
         *
         * @param evt the event details
         */
        void fileIndexMoved(FileIndexMovedEvent evt);
    }
}
