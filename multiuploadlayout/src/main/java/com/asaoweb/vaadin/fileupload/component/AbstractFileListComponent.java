package com.asaoweb.vaadin.fileupload.component;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.events.Events;
import com.asaoweb.vaadin.fileupload.ui.MultiUploadLayout;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractFileListComponent<FILE> extends HorizontalLayout {
    private final static Logger logger = LoggerFactory.getLogger(AbstractFileListComponent.class);
    protected final Image thumb = new Image();
    protected final Label filename = new Label();
    protected final Label mimeType = new Label();
    protected final Label fileSize = new Label();

    protected final Button action = new Button();

    protected final MultiUploadLayout layout;

    protected final AbstractLayout thumbWrapper;

    protected FILE file;

    protected boolean userInteractionsInited = false;

    public AbstractFileListComponent(MultiUploadLayout layout){
        this.layout = layout;

        thumb.addStyleName("thumb");
        filename.addStyleName("filename");
        mimeType.addStyleName("filetype");
        fileSize.addStyleName("filesize");
        action.setIcon(VaadinIcons.TRASH);
        action.setVisible(this.layout.isAllowDelete());
        action.addStyleName("action");

        if (layout.isCompactLayout()) {
            VerticalLayout compactWrapper = new VerticalLayout();
            compactWrapper.setMargin(false);
            compactWrapper.addStyleName("tusmultiuploadlayout-filelistcomponent-compact-wrapper");
            thumbWrapper = new CssLayout();
            thumbWrapper.addStyleName("tusmultiuploadlayout-filelistcomponent-compact-thumbwrapper");
            thumbWrapper.addComponent(thumb);
            thumbWrapper.addComponent(action);

            /*Map<Component, Consumer<Long>> extraComponentsToAdd = layout.addExtraComponents();
            if(!extraComponentsToAdd.isEmpty()){
                for(Component c : extraComponentsToAdd.keySet()) {
                    c.addListener(evt -> {
                        extraComponentsToAdd.get(c).accept(Long.valueOf(fileInfo.id));
                    });
                    thumbWrapper.addComponent(c);
                }
            }*/



            thumbWrapper.addComponent( fileSize);
            fileSize.setWidth("100%");

            thumb.setSizeFull();
            filename.setWidth("100%");
            //action.setWidth("auto");
            //action.setHeight("auto");

            compactWrapper.addComponents(thumbWrapper, filename);
            //this.setHeight(compactSize+20, Unit.PIXELS);
            this.addComponents(compactWrapper);
            this.addStyleName("tusmultiuploadlayout-filelistcomponent-compact");

        } else {
				/*thumb.setWidth(30, Unit.PIXELS);
				filename.setWidth(150, Unit.PIXELS);
				mimeType.setWidth(80, Unit.PIXELS);
				fileSize.setWidth(80, Unit.PIXELS);*/
            this.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
            VerticalLayout vLayout = new VerticalLayout();
            vLayout.setMargin(false);
            vLayout.setWidth("100%");
            thumbWrapper = new HorizontalLayout(thumb, filename, mimeType, fileSize, action);
            thumbWrapper.setWidth("100%");
            ((HorizontalLayout)thumbWrapper).setExpandRatio(filename, 1f);
            //vLayout.addComponents(thumbWrapper, statusWrapper);
            this.addComponents(thumbWrapper);
            this.setExpandRatio(thumbWrapper, 1.0f);
            this.setWidth("100%");
            this.addStyleName("tusmultiuploadlayout-filelistcomponent");
        }
    }

    public FILE getFile() {
        return file;
    }

    public void setFile(FILE file) {
        this.file = file;
        if (!userInteractionsInited && hasUserInteractions()){
            userInteractionsInited = true;
            attachDraggableExtension();
            addDeleteClickListener(e -> layout.fileDeletedClick(new Events.FileDeletedClickEvent<>(layout.getUploader(), getFile())));
        }
    }

    public boolean hasUserInteractions() {
        return this.file != null && !(this.file instanceof FileInfo);
    }

    protected void attachDraggableExtension(){
        if (layout.isAllowReorder()) {
            if (!layout.isCompactLayout()) {
                Label picker = new Label(VaadinIcons.ELLIPSIS_DOTS_V.getHtml());
                picker.setContentMode(ContentMode.HTML);
                this.addComponentAsFirst(picker);
            }

            DragSourceExtension<? extends AbstractFileListComponent> dragSourceExt = new DragSourceExtension<>(this);
            // set the allowed effect
            dragSourceExt.setEffectAllowed(EffectAllowed.MOVE);

            DropTargetExtension<? extends AbstractFileListComponent> dropTarget = new DropTargetExtension<>(this);
            dropTarget.setDropEffect(DropEffect.MOVE);
            dropTarget.addDropListener(event -> {
                // if the drag source is in the same UI as the target
                Optional<AbstractComponent> dragSource = event.getDragSourceComponent();
                logger.debug("addDropListener {} for source {}", event, dragSource);
                if (dragSource.isPresent() && dragSource.get() instanceof AbstractFileListComponent) {
                    AbstractFileListComponent sourceFlc = (AbstractFileListComponent) dragSource.get();

                    layout.onFileComponentMoveIndex(this, sourceFlc);
                }
            });

        }
    }



    public void addAction(Button action){
        thumbWrapper.addComponent(action);
    }

    public Registration addDeleteClickListener(Button.ClickListener listener){
        return action.addClickListener(listener);
    }

    protected void setValues(String filename, String filetype, Long filesize){
        this.filename.setValue(filename);
        this.mimeType.setValue(filetype);
        if (filesize > 0) {
            this.fileSize.setValue( UploadComponent.readableFileSize(filesize));
        } else {
            this.fileSize.setVisible(false);
        }
    }


}
