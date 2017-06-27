import React from 'react'
import ReactDOM from 'react-dom'
import moment from 'moment'
// import axios from 'axios'
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
    // const startTime = moment().subtract(90, 'minutes').valueOf()

    const data =
      [
        {'time': '2017-06-02T16:35:32.990', 'successes': 1, 'failures': 5},
        {'time': '2017-06-02T16:45:32.990', 'successes': 4, 'failures': 7},
        {'time': '2017-06-02T16:55:32.990', 'successes': 8, 'failures': 6},
        {'time': '2017-06-02T17:05:32.990', 'successes': 20, 'failures': 5},
        {'time': '2017-06-02T17:10:32.990', 'successes': 2, 'failures': 1},
        {'time': '2017-06-02T17:15:32.990', 'successes': 9, 'failures': 0},
        {'time': '2017-06-02T17:20:32.990', 'successes': 11, 'failures': 22}
      ]

    // axios.get(`/cas/status/stats/getAuthnAudit/summary?start=${startTime}&range=${this.props.range}&scale=${this.props.scale}`)
    // .then(res => {
    data.forEach(function (value) {
      value.time = moment(value.time).format('h:mm')
    })

    const graphData = data

    this.setState({graphData})
    // })
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
