import Uppy from '@uppy/core';
import Dashboard from '@uppy/dashboard';
import GoogleDrive from '@uppy/google-drive';
import Dropbox from '@uppy/dropbox';
import Instagram from '@uppy/instagram';
import Facebook from '@uppy/facebook';
import OneDrive from '@uppy/onedrive';
import Webcam from '@uppy/webcam';
import Url from '@uppy/url';
import ImageEditor from '@uppy/image-editor';
import AwsS3Multipart from '@uppy/aws-s3-multipart';
import French from '@uppy/locales/lib/fr_FR';
import Spanish from '@uppy/locales/lib/es_ES';

import '@uppy/core/dist/style.css';
import '@uppy/dashboard/dist/style.css';
import '@uppy/webcam/dist/style.css';
import '@uppy/screen-capture/dist/style.css';
import '@uppy/image-editor/dist/style.css';
import '@uppy/url/dist/style.css';


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

    var lastTime = [];

    var lastPercentProgress = [];

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
            let debug = state.debug;
            dashboardparameters.width=container.parentElement.offsetWidth;
            dashboardparameters.height=container.parentElement.offsetHeight-12;
            if (coreoptions.edomain == 'FR') {
                coreoptions.locale = French;
            } else if (coreoptions.edomain == 'ES') {
                coreoptions.locale = Spanish;
            }
            coreoptions.edomain = null;

            if(dashboardparameters.autoSize) {
                dashboardparameters.width = '100%';
                dashboardparameters.height = '100%';
            }

            if (debug) console.log("Initializing uppy");
            uppy = new Uppy(coreoptions)
                .use(Dashboard, dashboardparameters)
                .use(Webcam, { target: Dashboard })
                //.use(ScreenCapture, { target: Dashboard })
                .use(ImageEditor, { target: Dashboard })
                .use(AwsS3Multipart, { limit : 1000, companionUrl: companionUrl,
                    createMultipartUpload (file) {
                        return fetch(`${companionUrl}/s3/multipart`, {
                            method: 'post',
                            credentials: 'same-origin',
                            headers: {
                                Accept: 'application/json',
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                                filename: file.name,
                                type: file.type,
                                metadata: {}
                            })
                        }).then((response) => response.json())
                    }
                });
            if(dashboardparameters.plugins.includes("GoogleDrive")) uppy.use(GoogleDrive, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Dropbox")) uppy.use(Dropbox, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Instagram")) uppy.use(Instagram, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Facebook")) uppy.use(Facebook, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("OneDrive")) uppy.use(OneDrive, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Links")) uppy.use(Url, {target: Dashboard, companionUrl: companionUrl});


            uppy.on('restriction-failed', (file, error) => {
                if (debug)  console.log('Restriction failed', file)
                rpcProxy.onRestrictionFailed(this.safeSerialize(file),
                    this.safeSerialize(error));
            });

            uppy.on( 'upload', (data) => {
                if (debug)  console.log('Upload started : ' + data);
                rpcProxy.onUploadStarted(this.safeSerialize(data));
            });

            if (state.transferProgress) {
                uppy.on('upload-progress', (file, progress) => {
                    let newTime = Date.now();
                    let fileLastTime = 0;
                    if (lastTime[file.id]) {
                        fileLastTime = lastTime[file.id];
                    }
                    let fileLastPercentProgress = -1;
                    if (lastPercentProgress[file.id]) {
                        fileLastPercentProgress = lastPercentProgress[file.id];
                    }
                    // We notify the rpc server every second and every percent change maximum
                    if ((newTime - fileLastTime) > 1000 && (!progress.bytesTotal ||
                        100 * progress.bytesUploaded / progress.bytesTotal >= fileLastPercentProgress + 1)
                        || progress.bytesUploaded == progress.bytesTotal) {
                        if (debug) console.log("Progress stringify : " + this.safeStringify(progress));
                        rpcProxy.onUploadProgressUpdated(this.safeSerialize(file), this.safeSerialize(progress));
                        lastTime[file.id] = newTime;
                        if (progress.bytesTotal) {
                            lastPercentProgress[file.id] = 100 * progress.bytesUploaded / progress.bytesTotal;
                        }
                    }
                });

                uppy.on('file-added', (file) => {
                    if (debug) console.log('Added file', file)
                    rpcProxy.onFileAdded(this.safeSerialize(file));
                });

                uppy.on( 'upload-error', (file, error, response) => {
                    if (debug) console.log('File failure id ' + file.id, ' ; error : ' + error);
                    let fileSer = null;
                    if (file) {
                        fileSer = this.safeSerialize(file);
                    }
                    let errorSer = null;
                    if (error) {
                        errorSer = this.safeSerialize(error);
                    }
                    let responseSer = null;
                    if (response) {
                        responseSer = this.safeSerialize(response);
                    }
                    rpcProxy.onUploadError(fileSer, errorSer, responseSer);
                });
            }

            uppy.on( 'upload-success', (file, response) => {
                if (debug) console.log('File successfully uploaded ' + file.id);
                rpcProxy.onUploadSuccess(file, this.safeSerialize(response));
                uppy.removeFile(file.id);
            });

            uppy.on('complete', (result) => {
                if (debug) console.log("Upload complete! We’ve uploaded these files:", result.successful);
                if (debug) console.log("Failures:", result.failed);
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