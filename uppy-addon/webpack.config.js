var path = require('path');

var config = {
    devtool: 'source-map',
    entry: './src/frontend/uppy-component.js',
    output: {
        //path: path.resolve(__dirname, 'src/main/resources/com/asaoweb/client'),
        path: path.resolve(__dirname, 'src/main/webapp/VAADIN/addons/uppy-addon'),
        filename: 'bundle.uppy.min.js',
    },
    module: {
        rules: [
            {
                test: /\.(css)$/,
                include: [
                    path.resolve(__dirname, 'node_modules'),
                    path.resolve(__dirname, 'src/main/webapp/VAADIN/addons/uppy-addon'),
                ],
                use: [
                    'style-loader',
                    'css-loader'
                ]
            },
        ]
    },
};

module.exports = (env, argv) => {
    if (argv.mode === 'development') {
        //config.output.filename = 'bundle.uppy.js';
        config.devtool = 'inline-source-map';
    }

    return config;
};