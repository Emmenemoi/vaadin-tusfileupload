package com.asaoweb.vaadin.uppyfileupload.client.dashboard;

public class InlineDashboardParameters extends AbstractDashboardParameters {

    /**
     * https://uppy.io/docs/dashboard/#target-39-body-39
     * target selector
     * Dashboard is rendered into body, because it is hidden by default and only opened as a modal when trigger is clicked.
     */
    private String target = "body";

    /**
     * https://uppy.io/docs/dashboard/#width-750
     *
     * Width of the Dashboard in pixels. Used when inline: true.
     */
    private String width = "100%";

    /**
     * https://uppy.io/docs/dashboard/#height-550
     *
     * Height of the Dashboard in pixels. Used when inline: true.
     */
    private String height = "550px";

    //private boolean autoSize = true;

    public InlineDashboardParameters() {
        super();
        setInline(true);
        setTarget(".uppy-item-dashboard");
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    /*public boolean getAutoSize() { return autoSize; }

    public void setAutoSize(boolean autoSize) { this.autoSize = autoSize; }*/
}
