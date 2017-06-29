const path = require('path')
// const webpack = require('webpack');

module.exports = {
  context: __dirname,
  entry:
    {
      dashboard: './resources/static/js/components/dashboard/Dashboard',
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
    extensions: ['.js', '.json']
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
        include: path.resolve(__dirname, './resources/static/js'),
        test: /\.js$/,
        loader: 'babel-loader'
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
