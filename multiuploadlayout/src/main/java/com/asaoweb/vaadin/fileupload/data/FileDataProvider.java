package com.asaoweb.vaadin.fileupload.data;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.SerializablePredicate;

public interface FileDataProvider<FILES> extends DataProvider<FILES, SerializablePredicate<FILES>> {
}
