var path = require('path');

module.exports = {
    devtool: 'inline-source-map',
    entry: './src/main/webapp/VAADIN/addons/uppy-addon/uppy-component.js',
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
