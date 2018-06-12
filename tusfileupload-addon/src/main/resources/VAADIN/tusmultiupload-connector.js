com_asaoweb_vaadin_tusfileupload_component_TusMultiUpload = function () {

	var BROWSE_BUTTON_CAPTION = "Choose File";
	var BUTTON_CLASSNAME = "v-button v-widget";
	var BROWSE_BUTTON_CLASSNAME = "plupload-browse " + BUTTON_CLASSNAME;
	var SUBMIT_BUTTON_CLASSNAME = "plupload-submit " + BUTTON_CLASSNAME;
	var DEFAULT_STREAMING_PROGRESS_EVENT_INTERVAL_MS = 1000;
	  
	var t = this;
    var e = t.getElement();
    var s = t.getState();
    var d = true; /* debugger */
    
    /**
     * The div that contains the buttons and inputs for upload.
     * 
     * @type @exp;document@call;createElement
     */
    var container;

    /**
     * The div that acts as the browse for files button.
     * 
     * @type @exp;document@call;createElement
     */
    var browseBtn;
    
    /*
     * The RPC proxy to the server side implementation.
     */
    var rpcProxy = this.getRpcProxy();

    /*
     * The unique ID of the connector.
     */
    var connectorId = this.getConnectorId();
    
    /**
     * The input that holds the file uploads.
     * 
     * @type @exp;document@call;createElement
     */
    var fileInput;
    
    /**
     * The file queue.
     */
    var fileInputQueue = [];
    
    var fileInputQueuePosition = 0;

    /**
     * The TUS client object.
     */
    var uploader;
    
    /**
     * Store current uploader status.
     */
    var isUploading = false;
    
    /**
     * The last time a progress RPC call was sent to the server side. This is 
     * used to throttle the progress calls to prevent flooding the server side.
     * 
     * @type Number
     */
    var lastProgressRpc = 0;
    
    console_log('Creating component');
    dumpState();
    
    function dumpState() {
        console_log('Dumping shared state information....');
        console_log('ClientSideProgress: ' + s.clientSideProgress);
        console_log('Debug: ' + s.debug);

        console_log('Endpoint: ' + s.endpoint);
        console_log('Fingerprint: ' + s.fingerprint);
        console_log('Resume: ' + s.resume);
        console_log('chunkSize: ' + s.chunkSize);
        console_log('retryDelays: ' + s.retryDelays);
        console_log('removeFingerprintOnSuccess: ' + s.removeFingerprintOnSuccess);
        
        console_log('ownerId: ' + s.chunkSize);
        console_log('buttonCaption: ' + s.buttonCaption);
        console_log('mimeAccept: ' + s.mimeAccept);
        console_log('multiple: ' + s.multiple);
        console_log('maxFileSize: ' + s.maxFileSize);
        
    }
    
    /*
     * Simple method for logging to the JS console if one is available.
     */
    function console_log(msg) {
      if (window.console && d) {
        console.log("TusMultiUpload: "+msg);
      }
    };
    
    /**
     * Builds the container divs and the buttons in the div.
     * 
     * @param {type} state
     * @returns {undefined}
     */
    this._buildButtons = function(state) {
      // Container
      container = document.createElement("div");
      container.setAttribute("id", "tusmultiupload_container_" + connectorId);
      container.className = "tusmultiupload";
      e.appendChild(container);

      fileInput = document.createElement("input");
      fileInput.setAttribute("type", "file");
      fileInput.setAttribute("accept", state.mimeAccept);
      fileInput.setAttribute("multiple", state.multiple);
      fileInput.style.display = 'none';
      fileInput.addEventListener("change", function(e) {
          console_log("fileInput change: " + e.target.files);
    	  t._buildTusUploadQueue(e.target.files);  
    	  e.target.value = '';
    	});
      
      container.appendChild(fileInput);
      
      // Browse button.
      browseBtn = this._createPseudoVaadinButton();
      browseBtn.root.className = BROWSE_BUTTON_CLASSNAME;
      browseBtn.caption.innerHTML = BROWSE_BUTTON_CAPTION;
      browseBtn.root.enabled = true;
      browseBtn.root.onclick = function() { 
    	  fileInput.click(); 
      };
      container.appendChild(browseBtn.root);

      if (state.buttonCaption) {
    	  browseBtn.caption.innerHTML = state.buttonCaption;
      }
      
    };
        
    this._createPseudoVaadinButton = function() {

        var btn = document.createElement("div");
        btn.setAttribute("role", "button");
        btn.className = BUTTON_CLASSNAME;

        var btnWrap = document.createElement("span");
        btnWrap.className = "v-button-wrap";
        btn.appendChild(btnWrap);

        var btnCaption = document.createElement("span");
        btnCaption.className = "v-button-caption";
        btnCaption.innerHTML = "Button";
        btnWrap.appendChild(btnCaption);

        return {
          root: btn,
          wrap: btnWrap,
          caption: btnCaption
        };
    };
      
    this._buildTusUploadQueue = function(files) {
    	fileInputQueuePosition = 0;      
        var now = new Date().getTime();
    	for (i = 0; i < files.length; i++) {
    		var queueId = "queue-"+now+"-"+i;
        	fileInputQueue.push({id: queueId, file: files[i]});
    		rpcProxy.onQueuedFile( queueId, files[i].name, files[i].type, files[i].size);
    	}
    	// Create a new tus upload
    	if (!isUploading) {
    		var nextElement = fileInputQueue.shift();
    		t._buildTusUpload(nextElement);
    	}
    };
    
    this._buildTusUpload = function(fileQueue) {
        
    	uploader = new tus.Upload(fileQueue.file, {
	        endpoint: this.translateVaadinUri(s.endpoint),
	        retryDelays: [0, 1000, 3000, 5000],
	        resume: s.resume,
	        retryDelays: s.retryDelays,
	        chunkSize: s.chunkSize <= 0 ? Infinity : s.chunkSize,
	        removeFingerprintOnSuccess: s.removeFingerprintOnSuccess,
	        metadata: {
	            filename: fileQueue.file.name,
	            filetype: fileQueue.file.type,
	            owner: s.ownerId,
	            queueId: fileQueue.id
	        },
	        onError: function(error) {
	            console_log("Failed because: " + error);
	            isUploading = false;
	            lastProgressRpc = 0;
	            rpcProxy.onError( this.metadata.queueId, this.metadata.filename, this.metadata.filetype, error);
	        },
	        onProgress: function(bytesUploaded, bytesTotal) {
	        	if (s.clientSideProgress) {
		            var now = new Date().getTime();
		            if (lastProgressRpc + DEFAULT_STREAMING_PROGRESS_EVENT_INTERVAL_MS <= now) {
		            	var percentage = (bytesUploaded / bytesTotal * 100).toFixed(2);
			            console_log("onProgress "+bytesUploaded +"/"+ bytesTotal +": "+ percentage + "%");
			            isUploading = true;
			            lastProgressRpc = now;
			            rpcProxy.onProgress( this.metadata.queueId, this.metadata.filename, bytesUploaded, bytesTotal);
		            }
	        	}
	        },
	        onSuccess: function() {
	            console_log("Upload success "+ this.metadata.filename +" to "+ this.endpoint);
	            isUploading = false;
	            lastProgressRpc = 0;
	            rpcProxy.onFileUploaded( this.metadata.queueId, this.metadata.filename, this.metadata.filetype);
	            /* continue queue */
	            var nextElement = fileInputQueue.shift();
	            if( nextElement ) {
	            	t._buildTusUpload( nextElement );
	            }
	        }
	    });
    	
        rpcProxy.setNextQueuedFileIdAndStart( fileQueue.id);
    };
    
    /**
     * Called when the component is being unregistered (i.e. removed) from the UI. 
     * Cancel an in-progress uploads and destroy the uploader.
     * 
     * @returns {undefined}
     */
    this.onUnregister = function() {
      if (uploader) {
        console_log("Stopping and cleaning up component.");

        try {
          uploader.abort();
        }
        catch (ex) {
          // no op
        }

        uploader = null;
      }
    };
    
    /*
     * Called when the state on the server side changes. If the state 
     * changes require a rebuild of the upload component, it will be 
     * destroyed and recreated. All other state changes will be applied 
     * to the existing upload instance.
     */
    this.onStateChange = function() {

        s = this.getState();
        d = s.debug;
        
        console_log("State change!");

        if (!uploader || s.rebuild) {
          console_log("Building component for connector " + connectorId);

          // Cleanup the current uploader if there is one.
          uploader = null;
          e.innerHTML = "";

          try {
            // Build the new uploader.
            this._buildButtons(s);
          }
          catch (ex) {
            console_log(ex);
          }
        }
      };
    
    /**
     * Submits the upload if there is a file selected.
     * 
     * @returns {undefined}
     */
    this.submitUpload = function() {
        console_log("Starting upload due to server side submit."); 
        uploader.start();
    };
    
    /**
     * Resume the upload if there is a file in progress.
     * 
     * @returns {undefined}
     */
    this.resumeUpload = function() {
    	console_log("Resume upload due to server side submit."); 
        uploader.start();
    }
    /**
     * Pause the upload if there is a file in progress.
     * 
     * @returns {undefined}
     */
    this.pauseUpload = function() {
    	uploader.abort();
    	isUploading = false;
    };
    
    /**
     * Abort the upload if there is a file in progress.
     * 
     * @returns {undefined}
     */
    this.abortUpload = function() {
    	uploader.abort();
    	isUploading = false;
    };
    
    /**
     * Interrupts the upload if there is a file in progress.
     * 
     * @returns {undefined}
     */
    this.removeFromQueue = function(queueId) {
    	if (uploader && uploader.options.metadata.queueId == queueId) {
    		uploader.abort();
    	}
    	for (i = 0; i < fileInputQueue.length; i++) {
    		if (fileInputQueue[i].id == queueId) {
    			fileInputQueue.splice(i,1);
    		}
    	}
        // We have to generate a call to finalize the upload on the server side
        // because a manual stop won't generate any event.
        //rpcProxy.onError("", "", "", -1, null, "interrupted server side");
    };
    
    // -----------------------
    // Init component
    this.registerRpc("com.asaoweb.vaadin.tusfileupload.shared.TusMultiuploadClientRpc", this);
}