package com.asaoweb.vaadin.uppyfileupload.client.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class AbstractDashboardParameters implements Serializable {


    /**
     * https://uppy.io/docs/dashboard/#id-39-Dashboard-39
     * A unique identifier for this plugin. It defaults to 'Dashboard', but you can change this if you need multiple Dashboard instances.
     * Plugins that are added by the Dashboard get unique IDs based on this ID, like 'Dashboard:StatusBar' and 'Dashboard:Informer'
     */
    private String id = "Dashboard";

    /**
     * https://uppy.io/docs/dashboard/#metaFields
     *
     * An array of UI field objects that will be shown when a user clicks the “edit” button on that file. Configuring this enables the “edit” button on file cards. Each object requires:
     *
     * id, the name of the meta field. Note: this will also be used in CSS/HTML as part of the id attribute, so it’s better to avoid using characters like periods, semicolons, etc.
     * name, the label shown in the interface.
     * placeholder, the text shown when no value is set in the field. (Not needed when a custom render function is provided)
     * Optionally, you can specify render: ({value, onChange}, h) => void, a function for rendering a custom form element.
     * It gets passed ({value, onChange}, h) where value is the current value of the meta field, onChange: (newVal) => void is a function saving the new value and h is the createElement function from preact.
     * h can be useful when using uppy from plain JavaScript, where you cannot write JSX
     */
    /*
    metaFields: [],
     */

    /**
     * https://uppy.io/docs/dashboard/#plugins
     *
     * List of plugin IDs that should be shown in the Dashboard’s top bar. For example, to show the Webcam plugin:
     *
     * uppy.use(Webcam)
     * uppy.use(Dashboard, {
     *   plugins: ['Webcam']
     * })
     *
     * Of course, you can also use the target option in the Webcam plugin to achieve this. However, that does not work with the React components.
     *
     * The target option may be changed in the future to only accept DOM elements, so it is recommended to use this plugins array instead.
     */
    private final Collection<DashboardPlugin> plugins = new HashSet<>(Arrays.asList(DashboardPlugin.values()));

    /**
     * https://uppy.io/docs/dashboard/#inline-false
     *
     * By default, Dashboard will be rendered as a modal, which is opened by clicking on trigger.
     * If inline: true, Dashboard will be rendered into target and fit right in.
     */
    private boolean inline = false;

    /**
    * Waiting for the docs
    */
    private int thumbnailWidth = 280;

    /**
     * Waiting for the docs
     */
    //defaultTabIcon: defaultTabIcon,

    /**
     * https://uppy.io/docs/dashboard/#showLinkToFileUploadResult-true
     *
     * By default, when a file upload has completed, the file icon in the Dashboard turns into a link to the uploaded file.
     * If your app does not publicly store uploaded files or if it’s otherwise unwanted, pass showLinkToFileUploadResult: false.
     */
    private boolean showLinkToFileUploadResult = true;

    /**
     * https://uppy.io/docs/dashboard/#showProgressDetails-false
     *
     * Passed to the Status Bar plugin used in the Dashboard.
     *
     * By default, progress in Status Bar is shown as a simple percentage. If you would like to also display remaining upload size and time, set this to true.
     *
     * showProgressDetails: false: Uploading: 45%
     * showProgressDetails: true: Uploading: 45%・43 MB of 101 MB・8s left
     */
    private boolean showProgressDetails = false;

    /**
     * https://uppy.io/docs/dashboard/#hideUploadButton-false
     *
     * Passed to the Status Bar plugin used in the Dashboard.
     *
     * Hide the upload button. Use this if you are providing a custom upload button somewhere, and using the uppy.upload() API.
     */
    private boolean hideUploadButton = false;

    /**
     * https://uppy.io/docs/dashboard/#hideRetryButton-false
     *
     * Hide the retry button in StatusBar (the progress bar below the file list) and on each individual file.
     *
     * Use this if you are providing a custom retry button somewhere, and using the uppy.retryAll() or uppy.retryUpload(fileID) API.
     */
    private boolean hideRetryButton = false;

    /**
     * https://uppy.io/docs/dashboard/#hidePauseResumeButton-false
     *
     * Hide the pause/resume button (for resumable uploads, via tus, for example) in StatusBar and on each individual file.
     *
     * Use this if you are providing custom cancel or pause/resume buttons somewhere, and using the uppy.pauseResume(fileID) or uppy.removeFile(fileID) API.
     */
    private boolean hidePauseResumeButton = false;

    /**
     * https://uppy.io/docs/dashboard/#hideCancelButton-false
     *
     * Hide the cancel button in StatusBar and on each individual file.
     *
     * Use this if you are providing a custom retry button somewhere, and using the uppy.cancelAll() API.
     */
    private boolean hideCancelButton = false;

    /**
     * https://uppy.io/docs/dashboard/#hideProgressAfterFinish-false
     *
     * Hide Status Bar after the upload has finished.
     */
    private boolean hideProgressAfterFinish = false;

    /**
     * https://uppy.io/docs/dashboard/#doneButtonHandler
     *
     * This option is passed to the StatusBar, and will render a “Done” button in place of pause/resume/cancel buttons,
     * once the upload/encoding is done. The behaviour of this “Done” button is defined by the handler function —
     * can be used to close file picker modals or clear the upload state. This is what the Dashboard sets by default:
     *
     * doneButtonHandler: () => {
     *   this.uppy.reset()
     *   this.requestCloseModal()
     * }
     * Set to null to disable the “Done” button.
     */
    /*
    doneButtonHandler: () => {
        this.uppy.reset()
        this.requestCloseModal()
    },*/


    /**
     * https://uppy.io/docs/dashboard/#note-null
     *
     * Optionally, specify a string of text that explains something about the upload for the user.
     * This is a place to explain any restrictions that are put in place. For example: 'Images and video only, 2–3 files, up to 1 MB'.
     */
    private String note = null;

    /**
     * https://uppy.io/docs/dashboard/#disableStatusBar-false
     *
     * Dashboard ships with the StatusBar plugin that shows upload progress and pause/resume/cancel buttons.
     * If you want, you can disable the StatusBar to provide your own custom solution.
     */
    private boolean disableStatusBar = false;

    /**
     * https://uppy.io/docs/dashboard/#disableInformer-false
     *
     * Dashboard ships with the Informer plugin that notifies when the browser is offline, or when it is time to say cheese if Webcam is taking a picture.
     * If you want, you can disable the Informer and/or provide your own custom solution.
     */
    private boolean disableInformer = false;

    /**
     * https://uppy.io/docs/dashboard/#disableThumbnailGenerator-false
     *
     * Dashboard ships with the ThumbnailGenerator plugin that adds small resized image thumbnails to images, for preview purposes only.
     * If you want, you can disable the ThumbnailGenerator and/or provide your own custom solution.
     */
    private boolean disableThumbnailGenerator = false;

    /**
     * https://uppy.io/docs/dashboard/#fileManagerSelectionType-39-files-39
     *
     * Configure the type of selections allowed when browsing your file system via the file manager selection window.
     *
     * May be either ‘files’, ‘folders’, or ‘both’. Selecting entire folders for upload may not be supported on all browsers.
     */
    private FileSelectionType fileManagerSelectionType = FileSelectionType.files;

    /**
     * https://uppy.io/docs/dashboard/#proudlyDisplayPoweredByUppy-true
     *
     * Uppy is provided to the world for free by the team behind Transloadit. In return, we ask that you consider keeping a tiny Uppy logo at the bottom of the Dashboard, so that more people can discover and use Uppy.
     *
     * This is, of course, entirely optional. Just set this option to false if you do not wish to display the Uppy logo.
     */
    private boolean proudlyDisplayPoweredByUppy = true;

    /**
     * https://uppy.io/docs/dashboard/#waitForThumbnailsBeforeUpload-false
     *
     * Whether to wait for all thumbnails from @uppy/thumbnail-generator to be ready before starting the upload. If set to true,
     * Thumbnail Generator will envoke Uppy’s internal processing stage, displaying “Generating thumbnails…” message, and wait for thumbnail:all-generated event, before proceeding to the uploading stage.
     *
     * This is useful because Thumbnail Generator also adds EXIF data to images, and if we wait until it’s done processing, this data will be avilable on the server after the upload.
     */
    private boolean waitForThumbnailsBeforeUpload = false;

    /**
     *
     */
    //private onRequestCloseModal: () => this.closeModal();

    /**
     * https://uppy.io/docs/dashboard/#showSelectedFiles-true
     *
     * Show the list (grid) of selected files with preview and file name. In case you are showing selected files in your own app’s UI and want the Uppy Dashboard to just be a picker, the list can be hidden with this option.
     *
     * See also disableStatusBar option, which can hide the progress and upload button.
     */
    private boolean showSelectedFiles = true;

    /**
     * https://uppy.io/docs/dashboard/#showRemoveButtonAfterComplete-false
     *
     * Sometimes you might want to let users remove an uploaded file. Enabling this option only shows the remove X button in the Dashboard UI, but to actually send a request you should listen to file-removed event and add your logic there.
     *
     * <pre>{@code
     * uppy.on('file-removed', (file, reason) => {
     *   if (reason === 'removed-by-user') {
     *     sendDeleteRequestForFile(file)
     *   }
     * })
     * }</pre>
     * For an implementation example, please see #2301).
     */
    private boolean showRemoveButtonAfterComplete = false;

    /**
     *
     */
    //locale: defaultLocale,

    /**
     * https://uppy.io/docs/dashboard/#theme-39-light-39
     *
     * Uppy Dashboard supports “Dark Mode”. You can try it live on the Dashboard example page.
     *
     * There are three options:
     *
     * light — the default
     * dark
     * auto — will respect the user’s system settings and switch automatically
     */
    //private boolean browserBackButtonClose = false;

    private Theme theme =  Theme.light;

    /**
     * https://uppy.io/docs/dashboard/#autoOpenFileEditor-false
     *
     * Automatically open file editor (see @uppy/image-editor) for the first file in a batch. If one file is added, editor opens for that file, if 10 files are added — editor opens for the first file.
     *
     * Use case: user adds an image — Uppy opens Image Editor right away — user crops / adjusts the image — upload.
     */
    private boolean autoOpenFileEditor = false;

    /**
     * https://uppy.io/docs/dashboard/#replaceTargetContent-false
     *
     * Remove all children of the target element before mounting the Dashboard. By default, Uppy will append any UI to the target DOM element.
     *
     * This is the least dangerous option. However, there might be cases when you would want to clear the container element before placing Uppy UI in there (for example, to provide a fallback {@code <form>} that will be shown if Uppy or JavaScript is not available).
     * Set replaceTargetContent: true to clear the target before appending.
     */
    private boolean replaceTargetContent = false;

    public AbstractDashboardParameters() {
        setReplaceTargetContent(true);
        setProudlyDisplayPoweredByUppy(false);
        setShowLinkToFileUploadResult(false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<DashboardPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<DashboardPlugin> plugins) {
        this.plugins.clear();
        this.plugins.addAll(plugins);
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public boolean isShowLinkToFileUploadResult() {
        return showLinkToFileUploadResult;
    }

    public void setShowLinkToFileUploadResult(boolean showLinkToFileUploadResult) {
        this.showLinkToFileUploadResult = showLinkToFileUploadResult;
    }

    public boolean isShowProgressDetails() {
        return showProgressDetails;
    }

    public void setShowProgressDetails(boolean showProgressDetails) {
        this.showProgressDetails = showProgressDetails;
    }

    public boolean isHideUploadButton() {
        return hideUploadButton;
    }

    public void setHideUploadButton(boolean hideUploadButton) {
        this.hideUploadButton = hideUploadButton;
    }

    public boolean isHideRetryButton() {
        return hideRetryButton;
    }

    public void setHideRetryButton(boolean hideRetryButton) {
        this.hideRetryButton = hideRetryButton;
    }

    public boolean isHidePauseResumeButton() {
        return hidePauseResumeButton;
    }

    public void setHidePauseResumeButton(boolean hidePauseResumeButton) {
        this.hidePauseResumeButton = hidePauseResumeButton;
    }

    public boolean isHideCancelButton() {
        return hideCancelButton;
    }

    public void setHideCancelButton(boolean hideCancelButton) {
        this.hideCancelButton = hideCancelButton;
    }

    public boolean isHideProgressAfterFinish() {
        return hideProgressAfterFinish;
    }

    public void setHideProgressAfterFinish(boolean hideProgressAfterFinish) {
        this.hideProgressAfterFinish = hideProgressAfterFinish;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isDisableStatusBar() {
        return disableStatusBar;
    }

    public void setDisableStatusBar(boolean disableStatusBar) {
        this.disableStatusBar = disableStatusBar;
    }

    public boolean isDisableInformer() {
        return disableInformer;
    }

    public void setDisableInformer(boolean disableInformer) {
        this.disableInformer = disableInformer;
    }

    public boolean isDisableThumbnailGenerator() {
        return disableThumbnailGenerator;
    }

    public void setDisableThumbnailGenerator(boolean disableThumbnailGenerator) {
        this.disableThumbnailGenerator = disableThumbnailGenerator;
    }

    public FileSelectionType getFileManagerSelectionType() {
        return fileManagerSelectionType;
    }

    public void setFileManagerSelectionType(FileSelectionType fileManagerSelectionType) {
        this.fileManagerSelectionType = fileManagerSelectionType;
    }

    public boolean isProudlyDisplayPoweredByUppy() {
        return proudlyDisplayPoweredByUppy;
    }

    public void setProudlyDisplayPoweredByUppy(boolean proudlyDisplayPoweredByUppy) {
        this.proudlyDisplayPoweredByUppy = proudlyDisplayPoweredByUppy;
    }

    public boolean isWaitForThumbnailsBeforeUpload() {
        return waitForThumbnailsBeforeUpload;
    }

    public void setWaitForThumbnailsBeforeUpload(boolean waitForThumbnailsBeforeUpload) {
        this.waitForThumbnailsBeforeUpload = waitForThumbnailsBeforeUpload;
    }

    public boolean isShowSelectedFiles() {
        return showSelectedFiles;
    }

    public void setShowSelectedFiles(boolean showSelectedFiles) {
        this.showSelectedFiles = showSelectedFiles;
    }

    public boolean isShowRemoveButtonAfterComplete() {
        return showRemoveButtonAfterComplete;
    }

    public void setShowRemoveButtonAfterComplete(boolean showRemoveButtonAfterComplete) {
        this.showRemoveButtonAfterComplete = showRemoveButtonAfterComplete;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public boolean isAutoOpenFileEditor() {
        return autoOpenFileEditor;
    }

    public void setAutoOpenFileEditor(boolean autoOpenFileEditor) {
        this.autoOpenFileEditor = autoOpenFileEditor;
    }

    public boolean isReplaceTargetContent() {
        return replaceTargetContent;
    }

    public void setReplaceTargetContent(boolean replaceTargetContent) {
        this.replaceTargetContent = replaceTargetContent;
    }

    public enum FileSelectionType {

        files,
        folders,
        both;
    }

    public enum DashboardPlugin {
        GoogleDrive,
        Dropbox,
        OneDrive,
        Facebook,
        Instagram,
        Links
    }

    public enum Theme {

        light,
        dark,
        auto;
    }

    public class MetaField implements Serializable {

        private String id;

        private String name;

        private String placeholder;
    }

    public class RenderedMetaField extends MetaField {

        private MetaFieldRenderer renderer;
    }

    public class MetaFieldRenderer {

    }
}
