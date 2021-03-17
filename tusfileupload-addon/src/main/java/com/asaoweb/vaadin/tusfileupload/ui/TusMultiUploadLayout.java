package com.asaoweb.vaadin.tusfileupload.ui;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.fileupload.ui.MultiUploadLayout;
import com.asaoweb.vaadin.tusfileupload.component.TusMultiUpload;
import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;

import java.util.ArrayList;
import java.util.List;

public class TusMultiUploadLayout extends MultiUploadLayout {

    public TusMultiUploadLayout() throws TusException.ConfigError {
        this(null, new Config(), new ArrayList<>(), null, false);
    }
    public TusMultiUploadLayout(String buttonCaption) throws TusException.ConfigError {
        this(null, new Config(), new ArrayList<FileInfo>(), null, false);
    }

    public TusMultiUploadLayout(String buttonCaption, Config config, List<FileInfo> existingFiles, FileInfoThumbProvider provider,
                                boolean allowReorder) throws TusException.ConfigError {
        super(new TusMultiUpload(buttonCaption, config), existingFiles, provider, allowReorder);
    }
}
