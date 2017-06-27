import React from 'react'
import { VictoryChart, VictoryVoronoiContainer, VictoryLine, VictoryTheme } from 'victory'
const { array } = React.PropTypes

const AuthnAttemptsGraph = React.createClass({
  propTypes: {
    graphData: array
  },
  render: function () {
    return (
      <VictoryChart
        width={950} height={300}
        padding={50}
        containerComponent={<VictoryVoronoiContainer />}
        domainPadding={{y: 10}}
        theme={VictoryTheme.material}
      >
        <VictoryLine
          data={this.props.graphData}
          x='time'
          y='failures'
          style={{
            data: {stroke: 'tomato', opacity: 0.7},
            labels: {fontSize: 12},
            parent: {border: '1px solid #ccc'}
          }}
        />
        <VictoryLine
          data={this.props.graphData}
          x='time'
          y='successes'
          style={{
            data: {stroke: 'darkgreen', opacity: 0.7},
            labels: {fontSize: 12},
            parent: {border: '1px solid #ccc'}
          }}
        />
      </VictoryChart>
    )
  }
})

  // [{"time":"2017-06-02T17:35:32.990","successes":1,"failures":2}]
// ReactDOM.render(React.createElement(AuthnAttemptsGraph), document.getElementById('authn-graph'))
export default AuthnAttemptsGraph
