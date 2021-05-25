package com.asaoweb.vaadin.uppyfileupload.demo;

import javax.servlet.annotation.WebServlet;

import com.asaoweb.vaadin.uppyfileupload.ui.UppyMultiUpload;
import com.asaoweb.vaadin.fileupload.FileInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vaadin.annotations.*;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Theme("demo")
@Title("Vaadin 8 uppy integration add-on Demo")
@Widgetset("com.asaoweb.vaadin.uppyfileupload.demo.UppyDemoWidgetSet")
@Push(PushMode.DISABLED)
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
        String hello = "Hello";
        String hello2 = "dffd";
    }

    @Override
    protected void init(VaadinRequest request) {

        // Initialize our new UI component
        UIUid metas = new UIUid();
        metas.setId(UUID.randomUUID().toString());
        metas.setUserId(23348L);
        final UppyMultiUpload component = new UppyMultiUpload(metas,
                new ArrayList<FileInfo>(), null, true, "http://localhost:3020");

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.addComponent(component);
        layout.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
        setContent(layout);

      //  TimerTask task = new TimerTask() {
      //      @Override
      //      public void run() {

                getUI().access(new Runnable() {
                    @Override
                    public void run() {
                        component.getUploader().setDomain("ES");
                        //component.getUploader().setMaxFileSize(1000000L);
                        //component.getUploader().setAcceptFilter(Arrays.asList("image/*"));
                        component.getUploader().setClientSideDebug(true);
                        //component.getUploader().setMaxFileSize(128000);
                        //component.getUploader().setMaxFileCount(3);
                    }
                });
        //    }
        //};
        //Timer timer = new Timer();
        //timer.schedule(task, 7500L);
    }

    public final class UIUid implements Serializable {
        @JsonProperty("id")
        private String id;

        @JsonProperty("uId")
        private Long userId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}