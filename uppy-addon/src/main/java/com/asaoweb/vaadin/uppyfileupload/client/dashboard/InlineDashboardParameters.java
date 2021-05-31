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
    private int width = 750;

    /**
     * https://uppy.io/docs/dashboard/#height-550
     *
     * Height of the Dashboard in pixels. Used when inline: true.
     */
    private int height = 550;

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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
