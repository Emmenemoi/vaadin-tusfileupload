# vaadin-tusfileupload
Implement TUS protocol for File Uploads resuming within Vaadin apps
Implements Uppy to S3 multipart upload.

Inspired from:
- Server side for TUS protocol: https://github.com/terrischwartz/tus_servlet/
- Server side Vaadin: https://github.com/mpilone/html5-upload-vaadin

Using on client side for TUS protocol:
- https://github.com/tus/tus-js-client

Includes a native UI component for managing uploads.


To build uppy vaadin plugin, from folder uppy-addon :
1. npm install
2. npm run build
3. maven clean package

to build tus vaadin plugin, from folder tusfileupload-addon :
1. maven clean package