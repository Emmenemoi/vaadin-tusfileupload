package com.asaoweb.vaadin.fileupload.ui;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.asaoweb.vaadin.fileupload.component.AbstractFileListComponent;
import com.asaoweb.vaadin.fileupload.data.FileDataProvider;
import com.asaoweb.vaadin.fileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.component.UploadComponent;
import com.asaoweb.vaadin.fileupload.data.FileListComponentProvider;
import com.asaoweb.vaadin.fileupload.events.Events;
import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.fileupload.events.Events.AbstractTusUploadEvent;
import com.asaoweb.vaadin.fileupload.events.Events.FailedEvent;
import com.asaoweb.vaadin.fileupload.events.Events.FailedListener;
import com.asaoweb.vaadin.fileupload.events.Events.FileDeletedClickEvent;
import com.asaoweb.vaadin.fileupload.events.Events.FileDeletedClickListener;
import com.asaoweb.vaadin.fileupload.events.Events.FileIndexMovedEvent;
import com.asaoweb.vaadin.fileupload.events.Events.FileIndexMovedListener;
import com.asaoweb.vaadin.fileupload.events.Events.ProgressEvent;
import com.asaoweb.vaadin.fileupload.events.Events.ProgressListener;
import com.asaoweb.vaadin.fileupload.events.Events.StartedEvent;
import com.asaoweb.vaadin.fileupload.events.Events.StartedListener;
import com.asaoweb.vaadin.fileupload.events.Events.SucceededEvent;
import com.asaoweb.vaadin.fileupload.events.Events.SucceededListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;

public class MultiUploadLayout<FILES> extends VerticalLayout implements DataProviderListener<FILES>, FileDeletedClickListener<FILES> {
	  private final static Method FILE_DELETED_METHOD;
	  private final static Method FILE_MOVED_METHOD;

	  static {
		    try {
		    	FILE_DELETED_METHOD = FileDeletedClickListener.class.getMethod(
		          "fileDeletedClick", FileDeletedClickEvent.class);
		    	FILE_MOVED_METHOD = FileIndexMovedListener.class.getMethod(
				  "fileIndexMoved", FileIndexMovedEvent.class);
		    }
		    catch (NoSuchMethodException | SecurityException ex) {
		      throw new RuntimeException("Unable to find listener event method.", ex);
		    }
		  }
	  
	  private static final Logger logger = LoggerFactory.getLogger(MultiUploadLayout.class.getName());

	protected final Label infoLabel;
	protected final Panel listPanel;

	protected final SortedSet<FileInfo> queuedfiles = new TreeSet<>(Comparator.comparingLong(FileInfo::getIndex));
	protected final FileListComponentProvider<FILES> filelistItemComponentProvider;


	protected FileDataProvider<FILES> filesProvider;
	protected AbstractOrderedLayout fileListLayout;
	protected boolean 	allowReorder = false;
	protected boolean 	reverseOrder = false;
	protected boolean 	compactLayout = false;

	protected String 	infoLabelMessagePattern = "{0,,filenb} uploaded files / {2,,totalSize} (+{1,,queueSize} queued)";
	protected String	fileMinCountErrorMessagePattern = "Can't delete any file: {0,,minCount} files must be attached. Upload new files first.";
	protected String 	noFilesUploaded = "No files uploaded yet.";

	protected boolean 	allowDelete = true;
	protected int		minFileCount = 0;
	protected boolean   cachedHTML5DnD = false;
	protected HorizontalLayout infobar;

	protected UploadComponent uploadButton;

	protected Lock		updateLock = new ReentrantLock();
	private Registration dataListener = null;

	public MultiUploadLayout(UploadComponent uploadComponent,
													 FileDataProvider<FILES> existingFiles,
													 FileListComponentProvider<FILES> filelistItemComponentProvider,
													 boolean allowReorder) {
		super();
		this.filelistItemComponentProvider = filelistItemComponentProvider;
		infoLabel = new Label();
		listPanel = new Panel();
		listPanel.setSizeFull();
		infobar = new HorizontalLayout(infoLabel);
		infobar.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		infobar.setExpandRatio(infoLabel, 1f);
		
		this.setSizeFull();


		this.addStyleName("tusmultiuploadlayout");
		setUploader(uploadComponent);
		setFilesProvider(existingFiles);
		allowReorder(allowReorder);
	}

	public FileDataProvider<FILES> getFilesProvider() {
		return filesProvider;
	}

	public void setFilesProvider(FileDataProvider<FILES> filesProvider) {
		this.filesProvider = filesProvider;
		if (dataListener != null){
			dataListener.remove();
		}
		this.dataListener = filesProvider.addDataProviderListener(this);
		refreshFileList();
	}

	@Override
	public void onDataChange(DataChangeEvent<FILES> dataChangeEvent) {
		if (dataChangeEvent instanceof DataChangeEvent.DataRefreshEvent){
			refreshFileList();
		} else {
			refreshFileList();
		}
	}

	public void setUploaderLocation() {
		infobar.addComponentAsFirst(uploadButton);
		this.setExpandRatio(listPanel, 0.5f);
	}


	
	@Override
	public void attach() {
		refreshFileList();
		super.attach();
        if (allowReorder && getUI() != null) {
            cachedHTML5DnD = getUI().isMobileHtml5DndEnabled();
            // loads external polyfill: https://vaadin.com/docs/v8/framework/advanced/advanced-dragndrop.html
            // Drag and Drop is mutually exclusive with context click on mobile devices
            getUI().setMobileHtml5DndEnabled(true);
        }
	}

    @Override
    public void detach() {
	    super.detach();
        if (allowReorder && getUI() != null) {
            getUI().setMobileHtml5DndEnabled(cachedHTML5DnD);
        }
    }

	public Registration addSucceededListener(SucceededListener listener) {
		return uploadButton.addSucceededListener(listener);
	}
	public Registration addCompleteListener(Events.CompleteListener listener) {
		return uploadButton.addCompleteListener(listener);
	}
	
	public Registration addFileDeletedClickListener(FileDeletedClickListener listener) {
	    return addListener(FileDeletedClickEvent.class, listener, FILE_DELETED_METHOD);
	}

	public Registration addFileIndexMovedListener(FileIndexMovedListener listener) {
	    return addListener(FileIndexMovedEvent.class, listener, FILE_MOVED_METHOD);
	}

	@Override
	public void fileDeletedClick(FileDeletedClickEvent<FILES> evt) {
		fireEvent(evt);
		getFilesProvider().refreshAll();
	}

	public void refreshFileList() {
		if (compactLayout && (fileListLayout == null || fileListLayout instanceof VerticalLayout )) {
			fileListLayout = new HorizontalLayout();
			fileListLayout.setStyleName("tusmultiuploadlayout-float-container");
			fileListLayout.setSpacing(false);
			listPanel.setContent(fileListLayout);
		} else if ( !compactLayout && (fileListLayout == null || fileListLayout instanceof HorizontalLayout )){
			fileListLayout = new VerticalLayout();
			listPanel.setContent(fileListLayout);
		}
		
		fileListLayout.removeAllComponents();
		updateLock.lock();
		try {
			files().forEach(f -> addFileListComponent(filelistItemComponentProvider.getComponent(f, this)));
			queuedfiles.removeIf(f -> {
				if (f.isFinished()) {
					uploadButton.removeFromQueue(f.id);
					return true;
				}
				return false;
			});
			queuedfiles.forEach(this::addFileInfoItem);
		} finally {
			updateLock.unlock();
		}
		refreshFilesInfos();
	}

	protected Stream<FILES> files(){
		return filesProvider.fetch(new Query<>());
	}
	
	public void refreshFilesInfos() {
		long fileNB = files().count() + queuedfiles.size();
		long queueNB = 0;
		if (fileNB == 0 && fileListLayout.getComponentCount() == 0) {
			fileListLayout.addComponent(new Label(noFilesUploaded));
			queueNB = queueNB - 1;
		} else if (fileNB > 0 && fileListLayout.getComponentCount() >= 1) {
			Iterator<Component> iter = fileListLayout.iterator();
			while (iter.hasNext()){
				Component elem = iter.next();
				if (elem instanceof Label){
					iter.remove();
				}
			}
		}
		//last = fileListLayout.getComponentCount()-1;
		queueNB = queueNB + fileListLayout.getComponentCount() - fileNB;
		/*if (last >= 0  && fileListLayout.getComponent(last) instanceof Label) {
			queueNB--;
		}*/
		uploadButton.setRemainingQueueSeats(uploadButton.getMaxFileCount() != null ?
				uploadButton.getMaxFileCount()-fileNB : Integer.MAX_VALUE);
		long totalUploadedSize = queuedfiles.stream().mapToLong(fi -> fi.entityLength).sum();
		infoLabel.setValue( MessageFormat.format(infoLabelMessagePattern, fileNB, queueNB, UploadComponent.readableFileSize(totalUploadedSize) ));

	}

	public boolean hasUploadInProgress() {
		return this.getUploader().hasUploadInProgress();
	}

	public int getQueueCount() {
		/*int fileNB = files.size();
		int queueNB = fileListLayout.getComponentCount() - fileNB;*/
		int queueNB = this.getUploader().getQueueCount();
		logger.debug("getQueueCount: {}", queueNB);
		return queueNB;
	}

	public boolean hasRemainingQueue() {
		return getQueueCount() > 0;
	}

	/*
    public void replaceFileInfoItem(FileInfo originalFi, FileInfo newFi) {
	    if (originalFi == null || newFi == null) {
	        return;
        }
		updateLock.lock();
	    try {
			fileListLayout.forEach(fl -> {
				if (fl instanceof FileListComponent) {
					FileListComponent flc = (FileListComponent) fl;
					if (flc.fileInfo != null && flc.fileInfo.id != null && flc.fileInfo.id.equals(originalFi.id)) {
						flc.fileInfo = newFi;
					}
				}
			});
		} finally {
			updateLock.unlock();
		}
    }

    public void removeFileInfoItem(FileInfo fi) {
        if (fi.isUploading()) {
            logger.info("Won't delete {}: is uploading. Wait finish.", fi);
            return;
        }
        Iterator<Component> itr = this.fileListLayout.iterator();
        List<Component> tbd = new ArrayList<>();
		updateLock.lock();
		try {
			while (itr.hasNext()) {
				Component c = itr.next();
				if (c instanceof FileListComponent) {
					FileListComponent flc = (FileListComponent) c;
					if (flc.getFileInfo().equals(fi)) {
						if (flc.getFileInfo().isQueued()) {
							uploadButton.removeFromQueue(flc.getFileInfoQId());
						}
						tbd.add(c);
						files.remove(fi);
						break;
					}
				}
			}
		} finally {
			updateLock.unlock();
		}
        tbd.forEach(fileListLayout::removeComponent);
    }
*/
    private void addFileInfoItem(FileInfo fi) {
			FileListComponent f = new FileListComponent(fi, this);
			addFileListComponent(f);
		}

		protected void addFileListComponent(Component c) {
			if (fileListLayout.getComponentCount() > 0 && fileListLayout.getComponent(0) instanceof Label) {
				fileListLayout.removeComponent(fileListLayout.getComponent(0));
			}
			if (reverseOrder) {
				fileListLayout.addComponentAsFirst(c);
			} else {
				fileListLayout.addComponent(c);
			}
		}

		public void onFileComponentMoveIndex(AbstractFileListComponent<FILES> source, AbstractFileListComponent<FILES> destination){
			int targetIndex = fileListLayout.getComponentIndex(source);
			int currentIndex = fileListLayout.getComponentIndex(destination);
			if (targetIndex > currentIndex) {
				fileListLayout.addComponent(destination, targetIndex+1);
			} else {
				fileListLayout.addComponent(destination, targetIndex);
			}

			if (isReverseOrder()) {
				int maxIndex = fileListLayout.getComponentCount() - 1;
				fireEvent(new Events.FileIndexMovedEvent<>( uploadButton, source.getFile(), maxIndex - currentIndex, maxIndex - targetIndex));
			} else {
				fireEvent(new Events.FileIndexMovedEvent<>( uploadButton, source.getFile(), currentIndex, targetIndex));
			}
		}

	public AbstractOrderedLayout getFileListLayout() {
		return fileListLayout;
	}

	public void cancelSucceededEvent(SucceededEvent event) {
		updateLock.lock();
		try {
			this.queuedfiles.removeIf(f -> f.id != null && f.id.equals(event.getId()));
		} finally {
			updateLock.unlock();
		}
	    refreshFileList();
    }
	
	/**
	 * Default: "{0,,filenb} uploaded files / {2,,totalSize} (+{1,,queueSize} queued)"
	 * 
	 * @param pattern used in MessageFormat.format
	 */
	public void setInfoPanelMessagePattern(String pattern) {
		infoLabelMessagePattern = pattern;
	}
	
	public void setFileMinCountErrorMessagePattern(String pattern) {
		fileMinCountErrorMessagePattern = pattern;
	}
	
	public void setMinFileCount(int minFileCount) {
		this.minFileCount = minFileCount;
		if (fileListLayout != null && fileListLayout.isAttached()) {
			refreshFileList();
		}
	}

	public int getMaxFileCount() {
		return uploadButton.getMaxFileCount() != null ? uploadButton.getMaxFileCount() : Integer.MAX_VALUE;
	}

	public int getMinFileCount() {
		return minFileCount;
	}
	
	public void setChunkSize(long chunkSize) {
		uploadButton.setChunkSize(chunkSize);
	}
	
	public void setWithCredentials(boolean withCredentials) {
		uploadButton.setWithCredentials(withCredentials);
	}

	public void setUploader(UploadComponent uploadButton){
		if(this.uploadButton != null){
			this.uploadButton.detach();
		}
		this.uploadButton = uploadButton;
		setUploaderLocation();
		this.uploadButton.addFileQueuedListener(e -> {
			addFileInfoItem(e.getFileInfo());
			refreshFilesInfos();
		});
	}

	public UploadComponent getUploader() {
		return uploadButton;
	}
	public void allowReorder(boolean allowReorder) {
		this.allowReorder = allowReorder;
		if (fileListLayout != null && fileListLayout.isAttached()) {
			refreshFileList();
		}
	}
	public void setCompactLayout(boolean compactLayout) {
		this.compactLayout = compactLayout;
		if (fileListLayout != null && fileListLayout.isAttached()) {
			refreshFileList();
		}
	}
	/**
	 * Display file list in reverse order (last as first component)
	 */
	public void setReverseOrder(boolean reverseOrder) {
		this.reverseOrder = reverseOrder;
		if (fileListLayout != null && fileListLayout.isAttached()) {
			refreshFileList();
		}
	}
	public void setAllowDelete(boolean allowDelete) {
		this.allowDelete = allowDelete;
		if (fileListLayout != null && fileListLayout.isAttached()) {
			refreshFileList();
		}
	}

	public boolean isAllowReorder() {
		return allowReorder;
	}

	public boolean isAllowDelete() {
		return allowDelete;
	}

	public boolean isReverseOrder() {
		return reverseOrder;
	}

	public boolean isCompactLayout() {
		return compactLayout;
	}

	public static class FileListComponent extends AbstractFileListComponent implements FailedListener, ProgressListener, SucceededListener, StartedListener {
		protected static final String PROGRESS_STYLE = "progress";
		protected static final String FAILED_STYLE = "failed";


		protected final Label errorMessage = new Label();
		protected final ProgressBar progress = new ProgressBar();
		protected final VerticalLayout statusWrapper;
		protected final Label progressInfos = new Label();
		protected final AbstractOrderedLayout progressBarWrapper;
		protected final Lock flcUpdateLock = new ReentrantLock();

        protected FileInfo fileInfo;
        protected Registration rFailed, rStarted, rProgress, rSucceeded;

		public FileListComponent(FileInfo fileInfo, MultiUploadLayout layout) {
			super(layout);
			this.fileInfo = fileInfo;
			setValues(fileInfo.suggestedFilename, fileInfo.suggestedFiletype, fileInfo.entityLength);

			progress.setWidth("100%");
			progress.setStyleName("progress-bar");
			errorMessage.setVisible(false);
			errorMessage.setWidth("100%");
			progressInfos.addStyleName("progress-infos");

			progressBarWrapper = layout.compactLayout ? new VerticalLayout() : new HorizontalLayout();
			progressBarWrapper.addComponents(progress, progressInfos);
			progressBarWrapper.setVisible(false);
			progressBarWrapper.setMargin(false);
			progressBarWrapper.setSpacing(false);
			progressBarWrapper.setWidth("100%");
			progressBarWrapper.setExpandRatio(progress, 1.0f);

			statusWrapper = new VerticalLayout(errorMessage, progressBarWrapper);
			statusWrapper.setWidth("100%");
			statusWrapper.setMargin(false);
			statusWrapper.addStyleName("progress-wrapper");
			thumbWrapper.addComponent(statusWrapper);

			action.addClickListener((e) -> {
				if (this.fileInfo.isQueued()) {
					layout.getUploader().removeFromQueue(this.fileInfo.queueId);
				}
				if (this.fileInfo.isQueued()) {
					layout.updateLock.lock();
					try {
						layout.queuedfiles.remove(this.fileInfo);
					} finally {
						layout.updateLock.unlock();
					}
					//layout.fileListLayout.removeComponent(this);
					layout.refreshFilesInfos();
				}
			});
			

			
			if (fileInfo.isQueued()) {
				rFailed = layout.uploadButton.addFailedListener(this);
				rStarted = layout.uploadButton.addStartedListener(this);
				rProgress = layout.uploadButton.addProgressListener(this);
				rSucceeded = layout.uploadButton.addSucceededListener(this);
			}
			
			update();
		}
		
		@Override
		  public void detach() {
			unregisterListeners();
		    super.detach();
		  }
		
		protected boolean isImage() {
			return fileInfo.suggestedFiletype != null && fileInfo.suggestedFiletype.toLowerCase().contains("image");
		}

		protected boolean isVideo() {
			return fileInfo.suggestedFiletype != null && fileInfo.suggestedFiletype.toLowerCase().contains("video");
		}
		
		public void unregisterListeners() {
			if (rFailed != null) {
				rFailed.remove();
				rProgress.remove();
				rSucceeded.remove();
			}
		}
		
		public String getFileInfoQId() {
			return fileInfo.queueId;
		}
		
		public FileInfo getFileInfo() {
			return fileInfo;
		}
		
		public void update() {
			this.removeStyleName("ui-queued");
			this.removeStyleName("ui-finished");
			
			if (fileInfo.isQueued()) {
				this.addStyleName("ui-queued");
				progressBarWrapper.setVisible(true);
				action.setIcon(VaadinIcons.CLOSE);
			} else if (fileInfo.isFinished()) {
				this.addStyleName("ui-finished");
				action.setIcon(VaadinIcons.TRASH);
				progressBarWrapper.setVisible(false);
				action.setVisible(this.layout.allowDelete);
			}
			statusWrapper.setVisible(progressBarWrapper.isVisible() || errorMessage.isVisible());

			if( fileInfo.preview != null) {
				thumb.setSource(new ExternalResource(fileInfo.preview.toString()));
			} else if ( isImage() ) {
				thumb.setIcon(VaadinIcons.FILE_PICTURE);						
			} else if ( isVideo() ) {
				thumb.setIcon(VaadinIcons.FILE_MOVIE);
			} else {
				thumb.setIcon(VaadinIcons.FILE_O);
			}
		}
		
		public void setProgress(long value, long total) {
			flcUpdateLock.lock();
			try {
				fileInfo.offset = value;
				progress.setValue((float) value / (float) total);
				progress.setVisible(value>0);
				int pct = (int) ((float) value / (float) total * 100);
				if (this.layout.compactLayout) {
					progressInfos.setValue(UploadComponent.readableFileSize(value) + "/" + pct + "%");
				} else {
					progressInfos.setValue(UploadComponent.readableFileSize(value) + " / " + UploadComponent.readableFileSize(total) + " (" + pct + "%)");
				}
				errorMessage.setVisible(false);
				progressBarWrapper.setVisible(true);
				statusWrapper.setVisible(true);
				if (progress.getValue() >= 1) {
					update();
				}
			} finally {
				flcUpdateLock.unlock();
			}
		}
		
		public void setError(String message) {
			errorMessage.setVisible(false);
			progressBarWrapper.setVisible(false);
			statusWrapper.setVisible(true);
			errorMessage.setValue(message);
			this.addStyleName(FAILED_STYLE);
		}
		
		private boolean isEventOwner(AbstractTusUploadEvent evt) {
			logger.debug("FileListComponent.isEventOwner: {}(qId {}) for file component: {}", evt.getClass(), evt.getQueueId(), fileInfo);
			return fileInfo.queueId != null && fileInfo.queueId.equals(evt.getQueueId());
		}
		
		@Override
		public void uploadFailed(FailedEvent evt) {
			if ( isEventOwner(evt) ) {
				setError(evt.getReason().getMessage());
				unregisterListeners();
				this.removeStyleName(PROGRESS_STYLE);
				update();
			}
		}

		@Override
		public void uploadSucceeded(SucceededEvent evt) {
			if ( isEventOwner(evt) ) {
			    logger.debug("uploadSucceeded for evt {} (queue: {})", evt,this.layout.queuedfiles );
				flcUpdateLock.lock();
				try {
					if (evt.getFinalFileInfo() != null) {
						fileInfo = evt.getFinalFileInfo();
					} else if (evt.getId() != null && !evt.getId().isEmpty()) {
						fileInfo.id = evt.getId();
						fileInfo.offset = evt.getFileInfo().offset;
					}
					/*
					if (evt.shouldAddFileToList()) {
						this.layout.updateLock.lock();
						try {
							this.layout.queuedfiles.add(this.fileInfo);
						} finally {
							this.layout.updateLock.unlock();
						}
					}*/
				} finally {
					flcUpdateLock.unlock();
				}
				this.layout.queuedfiles.remove(this.fileInfo);
				this.layout.refreshFilesInfos();
				update();
				unregisterListeners();
				this.removeStyleName(PROGRESS_STYLE);

			}
		}

		@Override
		public void uploadProgress(ProgressEvent evt) {
			if ( isEventOwner(evt) ) {
				setProgress(evt.getFileInfo().offset, evt.getFileInfo().entityLength);
				this.addStyleName(PROGRESS_STYLE);
			}			
		}

		@Override
		public void uploadStarted(StartedEvent evt) {
			if ( isEventOwner(evt) ) {
				setProgress(evt.getFileInfo().offset, evt.getFileInfo().entityLength);
			}
		}
		
		
	}
}
