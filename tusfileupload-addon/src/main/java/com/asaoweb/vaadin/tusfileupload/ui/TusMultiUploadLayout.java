package com.asaoweb.vaadin.tusfileupload.ui;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.FileInfo;
import com.asaoweb.vaadin.tusfileupload.component.TusMultiUpload;
import com.asaoweb.vaadin.tusfileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.tusfileupload.events.Events.AbstractTusUploadEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.FailedEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.FailedListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.FileDeletedClickEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.FileDeletedClickListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.FileIndexMovedEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.FileIndexMovedListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.ProgressEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.ProgressListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.StartedEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.StartedListener;
import com.asaoweb.vaadin.tusfileupload.events.Events.SucceededEvent;
import com.asaoweb.vaadin.tusfileupload.events.Events.SucceededListener;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException.ConfigError;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;

public class TusMultiUploadLayout extends VerticalLayout {
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
	  
	  private static final Logger logger = LoggerFactory.getLogger(TusMultiUploadLayout.class.getName());

	protected final VerticalLayout fileListLayout;
	protected final TusMultiUpload uploadButton;
	protected final Label infoLabel;
	
	protected final List<FileInfo> files;
	
	protected boolean allowReorder = false;
	protected FileInfoThumbProvider provider;
	
	public TusMultiUploadLayout() throws ConfigError {
		this(null, new ArrayList<FileInfo>(), null, false);
	}	
	public TusMultiUploadLayout(String buttonCaption) throws ConfigError {
		this(null, new ArrayList<FileInfo>(), null, false);
	}
	
	public TusMultiUploadLayout(String buttonCaption, List<FileInfo> existingFiles, FileInfoThumbProvider provider, boolean allowReorder) throws ConfigError {
		super();
		uploadButton = new TusMultiUpload(buttonCaption, new Config());
		fileListLayout = new VerticalLayout();
		infoLabel = new Label();
		
		Panel listPanel = new Panel(fileListLayout);
		listPanel.setSizeFull();
		HorizontalLayout infobar = new HorizontalLayout(infoLabel, uploadButton);
		infobar.setExpandRatio(infoLabel, 1f);
		
		this.setHeight(300, Unit.PIXELS);
		this.setWidth(100, Unit.PERCENTAGE);
		this.addComponents(listPanel, infobar);
		this.setExpandRatio(listPanel, 1f);		
		files = existingFiles;
		
		uploadButton.addFileQueuedListener(e -> {
			fileListLayout.addComponent(new FileListComponent(e.getFileInfo(), uploadButton));
		});
		
		setThumbProvider(provider);
		allowReorder(allowReorder);
		
		refreshFileList();
	}
	
	public void setThumbProvider(FileInfoThumbProvider provider) {
		this.provider = provider;
	}
	
	public Registration addSucceededListener(SucceededListener listener) {
		return uploadButton.addSucceededListener(listener);
	}
	
	public Registration addFileDeletedClickListener(FileDeletedClickListener listener) {
	    return addListener(FileDeletedClickEvent.class, listener, FILE_DELETED_METHOD);
	}

	public Registration addFileIndexMovedListener(FileIndexMovedListener listener) {
	    return addListener(FileIndexMovedEvent.class, listener, FILE_MOVED_METHOD);
	}
	
	public void refreshFileList() {
		fileListLayout.removeAllComponents();
		files.forEach(f -> fileListLayout.addComponent(new FileListComponent(f, uploadButton)));
	}
	
	public void setChunkSize(long chunkSize) {
		uploadButton.setChunkSize(chunkSize);
	}
	
	public void allowReorder(boolean allowReorder) {
		this.allowReorder = allowReorder;
		if (fileListLayout.isAttached()) {
			refreshFileList();
		}
	}
	
	protected class FileListComponent extends HorizontalLayout implements FailedListener, ProgressListener, SucceededListener, StartedListener {
		protected final FileInfo fileInfo;
		
		protected final Image thumb = new Image();
		protected final Label filename = new Label();
		protected final Label mimeType = new Label();
		protected final Label fileSize = new Label();
		protected final Label errorMessage = new Label();
		protected final ProgressBar progress = new ProgressBar();
		protected final Button action = new Button();
		
		protected Registration rFailed, rStarted, rProgress, rSucceeded;
						
		public FileListComponent(FileInfo fileInfo, TusMultiUpload uploader) {
			super();
			this.fileInfo = fileInfo;	
			thumb.setWidth(30, Unit.PIXELS);
			filename.setWidth(150, Unit.PIXELS);
			filename.setValue(fileInfo.suggestedFilename);
			mimeType.setWidth(80, Unit.PIXELS);
			mimeType.setValue(fileInfo.suggestedFiletype);
			fileSize.setWidth(80, Unit.PIXELS);
			fileSize.setValue(readableFileSize(fileInfo.entityLength));
			progress.setWidth("100%");
			progress.setVisible(false);
			errorMessage.setVisible(false);
			errorMessage.setWidth("100%");
			HorizontalLayout progressWrapper = new HorizontalLayout(progress, errorMessage);
			progressWrapper.setSizeFull();
			
			//action.setWidth(30, Unit.PIXELS);
			action.addClickListener((e) -> {
				if (fileInfo.isQueued()) {
					uploadButton.removeFromQueue(this.fileInfo.queueId);
				} else {
					TusMultiUploadLayout.this.fireEvent(new FileDeletedClickEvent(this, this.fileInfo));
				}
				TusMultiUploadLayout.this.files.remove(this.fileInfo);
				TusMultiUploadLayout.this.fileListLayout.removeComponent(this);
			});
			
			this.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
			this.addComponents(thumb, filename, mimeType, fileSize, progressWrapper, action);
			this.setExpandRatio(progressWrapper, 1f);
			this.setWidth("100%");
			
			if (fileInfo.isQueued()) {
				rFailed = uploader.addFailedListener(this);
				rStarted = uploader.addStartedListener(this);
				rProgress = uploader.addProgressListener(this);
				rSucceeded = uploader.addSucceededListener(this);
			}
			
			if (TusMultiUploadLayout.this.allowReorder) {
				Label picker = new Label(VaadinIcons.ELLIPSIS_DOTS_V.getHtml());
				picker.setContentMode(ContentMode.HTML);
				this.addComponentAsFirst(picker);
				
				DragSourceExtension<FileListComponent> dragSourceExt = new DragSourceExtension<>(this);
				// set the allowed effect
				dragSourceExt.setEffectAllowed(EffectAllowed.MOVE);
				
				if (allowReorder) {
					DropTargetExtension<FileListComponent> dropTarget = new DropTargetExtension<>(this);
					dropTarget.setDropEffect(DropEffect.MOVE);
					dropTarget.addDropListener(event -> {
					    // if the drag source is in the same UI as the target
					    Optional<AbstractComponent> dragSource = event.getDragSourceComponent();
						logger.debug("addDropListener {} for source {}", event, dragSource);
					    if (dragSource.isPresent() && dragSource.get() instanceof FileListComponent) {
					    	FileListComponent sourceFlc = (FileListComponent) dragSource.get();

					    	int targetIndex = TusMultiUploadLayout.this.fileListLayout.getComponentIndex(this);
					    	int currentIndex = TusMultiUploadLayout.this.fileListLayout.getComponentIndex(sourceFlc);
					    	TusMultiUploadLayout.this.fileListLayout.addComponent(sourceFlc, targetIndex);
					    	TusMultiUploadLayout.this.fireEvent(new FileIndexMovedEvent(this, sourceFlc.getFileInfo(), currentIndex, targetIndex));
					    }
					});
				}
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
				action.setIcon(VaadinIcons.CLOSE);
			} else if (fileInfo.isFinished()) {
				this.addStyleName("ui-finished");
				action.setIcon(VaadinIcons.TRASH);
				progress.setVisible(false);
			}
			
			Resource thumbRsc;
			
			if ( provider != null && (thumbRsc = provider.getThumb(fileInfo)) != null ) {
				thumb.setIcon(null);
				thumb.setSource(thumbRsc);
			} else if ( isImage() ) {
				thumb.setIcon(VaadinIcons.FILE_PICTURE);						
			} else if ( isVideo() ) {
				thumb.setIcon(VaadinIcons.FILE_MOVIE);
			} else {
				thumb.setIcon(VaadinIcons.FILE_O);
			}
		}
		
		public void setProgress(long value, long total) {
			fileInfo.offset = value;
			progress.setValue( (float)value/(float)total);
			errorMessage.setVisible(false);
			progress.setVisible(true);
			if (progress.getValue() == 1) {
				update();
			}
		}
		
		public void setError(String message) {
			errorMessage.setVisible(false);
			progress.setVisible(false);
			errorMessage.setValue(message);
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
			}
		}

		@Override
		public void uploadSucceeded(SucceededEvent evt) {
			if ( isEventOwner(evt) ) {
				if ( evt.getId() != null && !evt.getId().isEmpty()) {
					fileInfo.id = evt.getId();
					fileInfo.offset = evt.getFileInfo().offset;
				}
				update();
				unregisterListeners();
			}
		}

		@Override
		public void uploadProgress(ProgressEvent evt) {
			if ( isEventOwner(evt) ) {
				setProgress(evt.getFileInfo().offset, evt.getFileInfo().entityLength);
			}			
		}

		@Override
		public void uploadStarted(StartedEvent evt) {
			if ( isEventOwner(evt) ) {
				setProgress(evt.getFileInfo().offset, evt.getFileInfo().entityLength);
			}
		}
		
		
	}
	
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
