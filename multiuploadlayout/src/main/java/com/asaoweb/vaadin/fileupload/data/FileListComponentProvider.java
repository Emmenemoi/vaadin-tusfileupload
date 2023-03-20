package com.asaoweb.vaadin.fileupload.data;

import com.asaoweb.vaadin.fileupload.ui.MultiUploadLayout;
import com.vaadin.ui.Component;

public interface FileListComponentProvider<FILES> {
    Component getComponent(FILES file, MultiUploadLayout<FILES> layout);
}
