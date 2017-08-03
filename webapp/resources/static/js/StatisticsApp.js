import React from 'react'
import ReactDOM from 'react-dom'
import axios from 'axios'
import AuthnAttemptsGraph from './components/authnaudit/AuthnAttemptsGraph'
const {string} = React.PropTypes

let refreshButtonStyle = {
  position: 'absolute',
  top: '0',
  right: '15px'
}

class StatisticsApp extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      start: '',
      graphData: [],
      refreshing: false
    }

    this.getData = this.getData.bind(this)
    this.refreshData = this.refreshData.bind(this)
  }

  refreshData (e) {
    this.setState({refreshing: true})
    this.getData()
  }

  getData () {
    let d = new Date()
    const startTime = d.setHours(d.getHours() - 2)
    axios.get(`/cas/status/stats/getAuthnAudit/summary?start=${startTime}&range=${this.props.range}&scale=${this.props.scale}`)
    .then(res => {
      res.data.forEach(function (value) {
        let newDate = new Date(value.time)
        value.time = (newDate.getHours() - (newDate.getHours() >= 12 ? 12 : 0)) + ':' + newDate.getMinutes()
      })
      const graphData = res.data
      this.setState({graphData, refreshing: false})
    })
  }

  componentDidMount () {
    this.getData()
  }
  render () {
    const refreshIcon = {
      fa: true,
      'fa-refresh': true,
      'fa-spin': this.state.refreshing
    }

    let button = null

    if (this.state.graphData.length < 2) {
      button = <button className='btn btn-primary btn-sm' disabled={this.state.refreshing} onClick={this.refreshData}><i className={refreshIcon} /> Refresh</button>
    } else {
      button = <button style={refreshButtonStyle} className='btn btn-default btn-xs' disabled={this.state.refreshing} onClick={this.refreshData}><i className={refreshIcon} /> Refresh</button>
    }

    return (
      <div>
        <h3>Authentication Attempts</h3>
        <AuthnAttemptsGraph graphData={this.state.graphData} />
        {button}
      </div>
    )
  }
}

StatisticsApp.propTypes = {
  scale: string,
  range: string
}

StatisticsApp.defaultProps = {
  range: 'PT03H',
  scale: 'PT05M'
}

ReactDOM.render(
  <StatisticsApp range='PT03H' scale='PT05M' />,
  document.getElementById('statistics-app')
)
