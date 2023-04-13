import Uppy from '@uppy/core';
import ThumbnailGenerator from '@uppy/thumbnail-generator';
import StatusBar from '@uppy/status-bar';
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
import Tus from '@uppy/tus';
import French from '@uppy/locales/lib/fr_FR';
import Spanish from '@uppy/locales/lib/es_ES';
import GoldenRetriever from '@uppy/golden-retriever';

import '@uppy/core/dist/style.css';
import '@uppy/dashboard/dist/style.css';
import '@uppy/webcam/dist/style.css';
import '@uppy/screen-capture/dist/style.css';
import '@uppy/image-editor/dist/style.css';
import '@uppy/url/dist/style.css';
import '@uppy/golden-retriever/lib/ServiceWorker';

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

    var autoHideThumbs = true;

    var lastTime = [];

    var lastPercentProgress = [];

    var containerId = "uppymultiupload_container_" + connectorId;

    /**
     * Builds the container divs and the buttons in the div.
     *
     * @param {type} state
     * @returns {undefined}
     */
    this._buildButtons = function(state) {
        container = document.createElement("div");
        container.setAttribute("id", containerId );
        container.className = "uppy-item-dashboard";
        e.appendChild(container);
        t.setSize(state.width, state.height)
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
            this.applyState(s);
        }
    };

    this.setSize = function(width, height) {
        if (width > 0) {
            container.width = width;
            e.width = width;
        }
        if (height > 0) {
            container.height = height;
            e.height = height;
        }
    };

    this.log = function (item, variables) {
        if (t.d) {
            console.dir("Uppy: ", item, variables);
        }
    }

    this.applyState = function(state){
        if (uppy) {
            uppy.setOptions(state.coreOptions);
            let dashboard = uppy.getPlugin('Dashboard')
            if (dashboard) {
                state.dashboardparameters.target = "#" + containerId;
                dashboard.setOptions(state.dashboardparameters);
            }
            let thumbnailGenerator = uppy.getPlugin('ThumbnailGenerator')
            if (thumbnailGenerator) {
                thumbnailGenerator.setOptions({
                    thumbnailWidth: state.dashboardparameters.thumbnailWidth
                });
            }
        }
    }

    this.registerRpc({
        init: function (state) {
            //let state = t.getState();
            let companionUrl = state.companionUrl;
            let dashboardparameters = state.dashboardparameters;
            dashboardparameters.target = "#" + containerId;
            let coreoptions = state.coreOptions;
            d = state.debug;
            if(e.offsetWidth > 0 && e.style.width == '100%' ) {
                dashboardparameters.width = e.offsetWidth;
            }
            if(e.offsetHeight > 0 &&  e.style.height == '100%' ) {
                dashboardparameters.height = e.offsetHeight - 12;
            }

            t.setSize(dashboardparameters.width, dashboardparameters.height);

            if (coreoptions.edomain == 'FR') {
                coreoptions.locale = French;
            } else if (coreoptions.edomain == 'ES') {
                coreoptions.locale = Spanish;
            }
            coreoptions.edomain = null;

            autoHideThumbs = coreoptions.autoHideThumbs;
            coreoptions.autoHideThumbs = null;

            t.log("Initializing uppy");
            uppy = new Uppy(coreoptions)
                .use(Dashboard, dashboardparameters)
                .use(Webcam, {target: Dashboard})
                .use(ThumbnailGenerator, {
                    thumbnailWidth: dashboardparameters.thumbnailWidth
                });

            if (state.allowImageEditor) {
                //.use(ScreenCapture, { target: Dashboard })
                uppy.use(ImageEditor, {target: Dashboard});
            }

            if(state.uploadModule == 'S3') {
                uppy.use(AwsS3Multipart, {
                    limit: 1000, companionUrl: companionUrl,
                    createMultipartUpload(file) {
                        // Only stings are allowes in metadata, we're settings in the s3 metadata only the ui id and the user id
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
                                metadata: new UIUid(file.meta.id, file.meta.userId)
                            })
                        }).then((response) => response.json())
                    }
                });
            } else if(state.uploadModule == 'TUS') {
                uppy.use(Tus, {
                    endpoint: `${companionUrl}/tus/files/`, // use your tus endpoint here
                    resume: true,
                    retryDelays: state.retryDelays
                });
            }

            if(dashboardparameters.plugins.includes("GoogleDrive")) uppy.use(GoogleDrive, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Dropbox")) uppy.use(Dropbox, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Instagram")) uppy.use(Instagram, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Facebook")) uppy.use(Facebook, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("OneDrive")) uppy.use(OneDrive, { target: Dashboard, companionUrl: companionUrl });
            if(dashboardparameters.plugins.includes("Links")) uppy.use(Url, {target: Dashboard, companionUrl: companionUrl});

            uppy.use(GoldenRetriever, {serviceWorker: true})
            if ('serviceWorker' in navigator) {
                navigator.serviceWorker
                    .register('/sw.js') // path to your bundled service worker with GoldenRetriever service worker
                    .then((registration) => {
                        t.log('ServiceWorker registration successful with scope: ', registration.scope)
                    })
                    .catch((error) => {
                        console.log('Registration failed with ' + error)
                    })
            }

            uppy.on('restriction-failed', (file, error) => {
                t.log('Restriction failed', file)
                rpcProxy.onRestrictionFailed(this.safeSerialize(file),
                    this.safeSerialize(error));
            });

            uppy.on( 'upload', (data) => {
                t.log('Upload started : ' + data);
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
                    t.log('Added file', file)
                    rpcProxy.onFileAdded(this.safeSerialize(file));
                });

                uppy.on( 'upload-error', (file, error, response) => {
                    t.log('File failure id ' + file.id, ' ; error : ' + error);
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
                t.log('File successfully uploaded ' + file.id);
                rpcProxy.onUploadSuccess(file, this.safeSerialize(response));
                // uppy.removeFile(file.id); // no complete fired
            });

            uppy.on('complete', (result) => {
                t.log("Upload complete! We’ve uploaded these files:", result.successful);
                t.log("Failures:", result.failed);
                rpcProxy.onUploadComplete(this.safeSerialize(result.successful), this.safeSerialize(result.failed));
            });

            uppy.on('thumbnail:generated', (file, preview) => {
                const img = document.createElement('img')
                img.src = preview
                img.width = t.getState().dashboardparameters.thumbnailWidth;
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
        cancelAll: function () {
            uppy.cancelAll();
        },
        initModal: function (opts) {
            this.init(opts);
        },
        initInline: function (opts) {
            this.init(opts);
        },
        setMeta: function (data) {
            if (uppy != undefined) {
                uppy.setMeta(data);
            }
        },
        resetDashboard: function () {
            /*if (uppy != undefined) {
                uppy.cancelAll();
            }*/
            if (container) {
                try {
                    container.getElementsByClassName("uppy-StatusBar-actionBtn--done")[0].click();
                } catch (ex) {
                    console.log(ex);
                }
            }
        },
        // safely handles circular references
        safeStringify: function(obj, indent = 2)  {
            if (obj) {
                let cache = [];
                const retVal = JSON.stringify(
                    obj,
                    (key, value) => {
                        if (key == 'uploader') {
                            return void (0);
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
            } else {
                return "{}";
            }
        },
        safeSerialize: function (obj) {
            return JSON.parse(this.safeStringify(obj));
        }
    });


    class UIUid {

        constructor(id, userId) {
            this.id = id;
            this.userId = userId;
        }
    }
}

