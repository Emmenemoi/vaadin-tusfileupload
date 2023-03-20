package com.asaoweb.vaadin.fileupload.data;

import com.vaadin.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.Collection;

public class ListFileDataProvider<FILES> extends ListDataProvider<FILES> implements FileDataProvider<FILES>{

    public ListFileDataProvider() {
        this(new ArrayList<>());
    }
    public ListFileDataProvider(Collection<FILES> items) {
        super(items);
    }


}
