import React from 'react'
import ReactDOM from 'react-dom'
import DashboardCircle from './DashboardCircle'

const JavaSucks = React.createClass({
  render: function () {
    return (
      <ul>
        <DashboardCircle url='/cas/status' title='Status' tooltip='Status view' />
        <DashboardCircle url='/cas/status/stats' title='Statistics Panel' tooltip='Statistics view' />
        <DashboardCircle url='/cas/status/config' title='Configuration Panel' tooltip='Config view' />
      </ul>
    )
  }
})

ReactDOM.render(React.createElement(JavaSucks), document.getElementById('app-launcher'))
