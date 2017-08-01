const path = require('path')
// const webpack = require('webpack');

const config = {
  context: __dirname,
  entry:
    {
      statistics: './resources/static/js/StatisticsApp'
    },
  devtool: 'eval',
  output: {
    path: path.join(__dirname, '/resources/static/js'),
    filename: '[name]-bundle.js'
  },
  devServer: {
    publicPath: './resources/static/',
    historyApiFallback: true
  },
  resolve: {
    extensions: ['.js', '.json'],
    alias: {
      react: 'preact-compat',
      'react-dom': 'preact-compat'
    }
  },
  stats: {
    colors: true,
    reasons: true,
    chunks: true
  },
  // plugins: [
  //   // Ignore all locale files of moment.js
  //   new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
  //   new BundleAnalyzerPlugin()
  // ],
  module: {
    rules: [
      {
        enforce: 'pre',
        test: /\.js$/,
        loader: 'eslint-loader',
        exclude: /node_modules/
      },
      {
        test: /\.json$/,
        loader: 'json-loader'
      },
      {
        include: [
          path.resolve(__dirname, './resources/static/js'),
          path.resolve(__dirname, './node_modules/preact-compat/src')
        ],
        test: /\.js$/,
        loader: 'babel-loader'
        // include: [
        //   path.resolve('js'),
        // ]
      },
      {
        test: /\.css$/,
        use: [
          'style-loader',
          {
            loader: 'css-loader',
            options: {
              url: false
            }
          }
        ]
      }
    ]
  }
}

if (process.env.NODE_ENV === 'production') {
  config.entry = {
    statistics: './resources/static/js/StatisticsApp'
  }
  config.devtool = false;
  config.plugins = []

}
module.exports = config
