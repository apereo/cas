import React from 'react'
import ReactDOM from 'react-dom'
import axios from 'axios'
import AuthnAttemptsGraph from './components/authnaudit/AuthnAttemptsGraph'
const {string} = React.PropTypes

class StatisticsApp extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      start: '',
      graphData: []
    }
  }

  componentDidMount () {
    let d = new Date()
    const startTime = d.setHours(d.getHours() - 8)

    axios.get(`/cas/status/stats/getAuthnAudit/summary?start=${startTime}&range=${this.props.range}&scale=${this.props.scale}`)
    .then(res => {
      res.data.forEach(function (value) {
        let newDate = new Date(value.time)
        value.time = (newDate.getHours() - (newDate.getHours() >= 12 ? 12 : 0)) + ':' + newDate.getMinutes()
      })
      const graphData = res.data
      this.setState({graphData})
    })
  }

  render () {
    return (
      <div>
        <h3>Authentication Attempts</h3>
        <AuthnAttemptsGraph graphData={this.state.graphData} />
      </div>
    )
  }
}

StatisticsApp.propTypes = {
  scale: string,
  range: string
}

ReactDOM.render(
  <StatisticsApp range='PT20H' scale='PT05M' />,
  document.getElementById('authn-graph')
)
