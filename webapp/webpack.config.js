const path = require('path')

const config = {
  context: __dirname,
  entry: {
    statistics: './resources/static/js/StatisticsApp'
  },
  output: {
    path: path.join(__dirname, '/resources/static/js'),
    filename: '[name]-bundle.js'
  },
  devServer: {
    publicPath: './resources/static/',
    historyApiFallback: true
  },
  resolve: {
    extensions: ['.js', '.jsx', '.json'],
    "alias": {
      "react": "preact-compat",
      "react-dom": "preact-compat"
    }
  },
  stats: {
    colors: true
  },
  module: {
    rules: [
      {
        enforce: 'pre',
        test: /\.jsx?$/,
        loader: 'eslint-loader',
        exclude: /node_modules/
      },
      {
        include: [
          path.resolve(__dirname, './resources/static/js'),
          path.resolve(__dirname, './node_modules/preact-compat/src')
        ],
        test: /\.jsx?$/,
        loader: 'babel-loader'
      }
    ]
  }
}

module.exports = config
