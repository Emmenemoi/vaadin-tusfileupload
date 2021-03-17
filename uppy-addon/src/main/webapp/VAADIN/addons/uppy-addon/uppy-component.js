import Uppy from '@uppy/core';
import Dashboard from '@uppy/dashboard';
import GoogleDrive from '@uppy/google-drive';
import Dropbox from '@uppy/dropbox';
import Instagram from '@uppy/instagram';
import Facebook from '@uppy/facebook';
import OneDrive from '@uppy/onedrive';
import Webcam from '@uppy/webcam';
import ScreenCapture from '@uppy/screen-capture';
import ImageEditor from '@uppy/image-editor';
import AwsS3Multipart from '@uppy/aws-s3-multipart';
import French from '@uppy/locales/lib/fr_FR';
import Spanish from '@uppy/locales/lib/es_ES';

import '@uppy/core/dist/style.css';
import '@uppy/dashboard/dist/style.css';
import '@uppy/webcam/dist/style.css';
import '@uppy/screen-capture/dist/style.css';
import '@uppy/image-editor/dist/style.css';

window.com_asaoweb_vaadin_uppyfileupload_UppyUploaderComponent  = function() {

    /*
    * The RPC proxy to the server side implementation.
    */
    var rpcProxy = this.getRpcProxy();

    var uppy = null;

    var connectorId = this.getConnectorId();

    /**
     * The div that contains the buttons and inputs for upload.
     *
     * @type @exp;document@call;createElement
     */
    var container;

    var t = this;
    var e = t.getElement();
    var d = true; /* debugger */

    /**
     * The TUS client object.
     */
    var uploader;

    /**
     * Store current uploader status.
     */
    var isUploading = false;

    /**
     * Builds the container divs and the buttons in the div.
     *
     * @param {type} state
     * @returns {undefined}
     */
    this._buildButtons = function(state) {
        container = document.createElement("div");
        container.setAttribute("id", "uppymultiupload_container_" + connectorId);
        container.className = "uppy-item-dashboard";
        container.width = "100%";
        e.appendChild(container);
    }

    /*
 * Called when the state on the server side changes. If the state
 * changes require a rebuild of the upload component, it will be
 * destroyed and recreated. All other state changes will be applied
 * to the existing upload instance.
 */
    this.onStateChange = function(e) {

        let s = this.getState();
        d = s.debug;

        if ( !container) {


            // Cleanup the current uploader if there is one.
            // uploader = null; // not needed: recreated for each file upload
            e.innerHTML = "";

            try {
                // Build the new uploader.
                this._buildButtons(s);
            } catch (ex) {
                console.log(ex);
            }
        } else {
            if (uppy) {
                uppy.setOptions(s.coreOptions);
            }
        }
    };

    this.registerRpc({
        init: function () {
            let state = t.getState();
            let companionUrl = state.companionUrl;
            let dashboardparameters = state.dashboardparameters;
            let coreoptions = state.coreOptions;
            dashboardparameters.width=container.parentElement.offsetWidth;
            dashboardparameters.height=container.parentElement.offsetHeight-12;
            if (coreoptions.edomain == 'FR') {
                coreoptions.locale = French;
            } else if (coreoptions.edomain == 'ES') {
                coreoptions.locale = Spanish;
            }
            coreoptions.edomain = null;

            console.log("Initializing uppy");
            uppy = new Uppy(coreoptions)
                .use(Dashboard, dashboardparameters)
                .use(GoogleDrive, { target: Dashboard, companionUrl: companionUrl })
                .use(Dropbox, { target: Dashboard, companionUrl: companionUrl })
                .use(Instagram, { target: Dashboard, companionUrl: companionUrl })
                .use(Facebook, { target: Dashboard, companionUrl: companionUrl })
                .use(OneDrive, { target: Dashboard, companionUrl: companionUrl })
                .use(Webcam, { target: Dashboard })
                .use(ScreenCapture, { target: Dashboard })
                .use(ImageEditor, { target: Dashboard })
                .use(AwsS3Multipart, { limit : 2, companionUrl: companionUrl, metaField: [ 'id', 'uId'],
                    getKey: (req, filename, metadata) => `${metadata.uId}/123/${filename}`});

            uppy.on('file-added', (file) => {
                console.log('Added file', file)
                rpcProxy.onFileAdded(this.safeSerialize(file));
            });

            uppy.on('restriction-failed', (file, error) => {
                console.log('Restriction failed', file)
                rpcProxy.onRestrictionFailed(this.safeSerialize(file),
                    this.safeSerialize(error));
            });

            uppy.on( 'upload', (data) => {
                console.log('Upload started : ' + data);
                rpcProxy.onUploadStarted(this.safeSerialize(data));
            });

            uppy.on('progress', (progress) => {
                console.log('Progress updated : ' + progress);
                rpcProxy.onProgressUpdated(progress);
            });

            uppy.on('upload-progress', (file, progress) => {
                //console.log('Progress updated for file ' + JSON.stringify(file) + ' : ' + JSON.stringify(progress));
                console.log("Progress stringify : " +
                    this.safeStringify(progress));
                rpcProxy.onUploadProgressUpdated(this.safeSerialize(file), this.safeSerialize(progress));
            });

            uppy.on( 'upload-success', (file, response) => {
                console.log('File successfully uploaded ' + file.id);
                rpcProxy.onUploadSuccess(file, this.safeSerialize(response));
                uppy.removeFile(file.id);
            });

            uppy.on( 'upload-error', (file, error, response) => {
                console.log('File failure id ' + file.id, ' ; error : ' + error);
                rpcProxy.onUploadError(this.safeSerialize(file),
                    this.safeSerialize(error),
                    this.safeSerialize(response));
            })

            uppy.on('complete', (result) => {
                console.log("Upload complete! We’ve uploaded these files:", result.successful);
                console.log("Failures:", result.failed);
                rpcProxy.onUploadComplete(this.safeSerialize(result.successful), this.safeSerialize(result.failed));
            });

            uppy.on('thumbnail:generated', (file, preview) => {
                const img = document.createElement('img')
                img.src = preview
                img.width = 100
                document.body.appendChild(img)
            })

            uppy.getFiles().forEach(file => {
                uppy.setFileState(file.id, {
                    progress: { uploadComplete: true, uploadStarted: false }
                })
            })
        },
        removeFile: function (fileId) {
            uppy.removeFile(fileId);
        },
        initModal: function () {
            this.init();
        },
        initInline: function () {
            this.init();
        },
        setMeta: function (data) {
            if (uppy != undefined) {
                uppy.setMeta(data);
            }
        },
        // safely handles circular references
        safeStringify: function(obj, indent = 2)  {
            let cache = [];
            const retVal = JSON.stringify(
                obj,
                (key, value) => {
                    if (key == 'uploader') {
                        return void(0);
                    }
                    var returnValue = typeof value === "object" && value !== null
                        ? cache.includes(value)
                        ? undefined // Duplicate reference found, discard key
                        : cache.push(value) && value // Store value in our collection
                        : value
                    return returnValue;
                },
                indent
            );
            cache = null;
            return retVal;
        },
        safeSerialize: function (obj) {
            return JSON.parse(this.safeStringify(obj));
        }
    });


}