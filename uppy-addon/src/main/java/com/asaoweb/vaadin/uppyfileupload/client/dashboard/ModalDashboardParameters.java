package com.asaoweb.vaadin.uppyfileupload.client.dashboard;

public class ModalDashboardParameters extends AbstractDashboardParameters {

    /**
     * https://uppy.io/docs/dashboard/#trigger-39-uppy-select-files-39
     *
     * String with a CSS selector for a button that will trigger opening the Dashboard modal. Multiple buttons or links
     * can be used, as long as it is a class selector (.uppy-choose, for example).
     */
    private String trigger = "#uppy-select-files";

    /**
     * https://uppy.io/docs/dashboard/#closeModalOnClickOutside-false
     *
     * Set to true to automatically close the modal when the user clicks outside of it.
     */
    private boolean closeModalOnClickOutside = false;

    /**
     * https://uppy.io/docs/dashboard/#closeAfterFinish-false
     *
     * Set to true to automatically close the modal when all current uploads are complete. You can use this together with the allowMultipleUploads: false option in Uppy Core to create a smooth experience when uploading a single (batch of) file(s).
     *
     * With this option, the modal is only automatically closed when uploads are complete and successful. If some uploads failed, the modal stays open so the user can retry failed uploads or cancel the current batch and upload an entirely different set of files instead.
     *
     * Setting allowMultipleUploads: false is strongly recommended when using this option. With multiple upload batches, the auto-closing behavior can be very confusing for users.
     */
    private boolean closeAfterFinish = false;

    /**
     * https://uppy.io/docs/dashboard/#disablePageScrollWhenModalOpen-true
     *
     * Page scrolling is disabled by default when the Dashboard modal is open, so when you scroll a list of files in Uppy,
     * the website in the background stays still. Set to false to override this behaviour and leave page scrolling intact.
     */
    private boolean disablePageScrollWhenModalOpen = true;

    /**
     * https://uppy.io/docs/dashboard/#animateOpenClose-true
     *
     * Add light animations when the modal dialog is opened or closed, for a more satisfying user experience.
     */
    private boolean animateOpenClose = true;

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public boolean isCloseModalOnClickOutside() {
        return closeModalOnClickOutside;
    }

    public void setCloseModalOnClickOutside(boolean closeModalOnClickOutside) {
        this.closeModalOnClickOutside = closeModalOnClickOutside;
    }

    public boolean isCloseAfterFinish() {
        return closeAfterFinish;
    }

    public void setCloseAfterFinish(boolean closeAfterFinish) {
        this.closeAfterFinish = closeAfterFinish;
    }

    public boolean isDisablePageScrollWhenModalOpen() {
        return disablePageScrollWhenModalOpen;
    }

    public void setDisablePageScrollWhenModalOpen(boolean disablePageScrollWhenModalOpen) {
        this.disablePageScrollWhenModalOpen = disablePageScrollWhenModalOpen;
    }

    public boolean isAnimateOpenClose() {
        return animateOpenClose;
    }

    public void setAnimateOpenClose(boolean animateOpenClose) {
        this.animateOpenClose = animateOpenClose;
    }
}
