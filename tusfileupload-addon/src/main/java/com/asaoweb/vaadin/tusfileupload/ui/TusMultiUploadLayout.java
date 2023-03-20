package com.asaoweb.vaadin.tusfileupload.ui;

import com.asaoweb.vaadin.fileupload.FileInfo;
import com.asaoweb.vaadin.fileupload.data.FileDataProvider;
import com.asaoweb.vaadin.fileupload.data.FileInfoThumbProvider;
import com.asaoweb.vaadin.fileupload.data.FileListComponentProvider;
import com.asaoweb.vaadin.fileupload.data.ListFileDataProvider;
import com.asaoweb.vaadin.fileupload.ui.MultiUploadLayout;
import com.asaoweb.vaadin.tusfileupload.component.TusMultiUpload;
import com.asaoweb.vaadin.tusfileupload.Config;
import com.asaoweb.vaadin.tusfileupload.exceptions.TusException;

import java.util.ArrayList;
import java.util.List;

public class TusMultiUploadLayout<FILES> extends MultiUploadLayout<FILES> {

    public TusMultiUploadLayout(FileListComponentProvider<FILES> filelistItemComponentProvider) throws TusException.ConfigError {
        this(null, new Config(), new ListFileDataProvider<>(), filelistItemComponentProvider, false);
    }
    public TusMultiUploadLayout(String buttonCaption, FileListComponentProvider<FILES> filelistItemComponentProvider) throws TusException.ConfigError {
        this(null, new Config(), new ListFileDataProvider<>(), filelistItemComponentProvider, false);
    }

    public TusMultiUploadLayout(String buttonCaption, Config config, FileDataProvider<FILES> existingFiles, FileListComponentProvider<FILES> filelistItemComponentProvider,
                                boolean allowReorder) throws TusException.ConfigError {
        super(new TusMultiUpload(buttonCaption, config), existingFiles, filelistItemComponentProvider, allowReorder);
    }
}
