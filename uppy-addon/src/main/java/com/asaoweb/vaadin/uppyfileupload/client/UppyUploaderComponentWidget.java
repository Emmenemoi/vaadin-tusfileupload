package com.asaoweb.vaadin.uppyfileupload.client;

import com.google.gwt.user.client.ui.Label;

// Extend any GWT Widget
public class UppyUploaderComponentWidget extends Label {

    // State can have both public variable and bean properties
    public String WIDGET_NAME = "UppyUploaderComponent";

    public UppyUploaderComponentWidget() {

        // CSS class-name should not be v- prefixed
        addStyleName("uppy-item-dashboard");
        setText(WIDGET_NAME);
        // State is set to widget in UppyComponentConnector
    }


}