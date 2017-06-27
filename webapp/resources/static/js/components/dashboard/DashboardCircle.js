import React from 'react'
const { string } = React.PropTypes

const DashboardCircle = React.createClass({
  propTypes: {
    title: string,
    tooltip: string,
    url: string
  },

  render: function () {
    return (
      <li>
        <a href={this.props.url} className='casTooltip'>{this.props.title}
          <span className='tooltiptext'>{this.props.tooltip}</span>
        </a>
      </li>
    )
  }
})

export default DashboardCircle
