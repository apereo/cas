import React from 'react'
import { VictoryChart, VictoryVoronoiContainer, VictoryLine } from 'victory'
const { array } = React.PropTypes

const AuthnAttemptsGraph = React.createClass({
  propTypes: {
    graphData: array
  },
  render: function () {
    if (this.props.graphData.length < 2) {
      return (
        <div>
          <h4>No data found</h4>
          <button onClick='refreshHandler' className='btn btn-primary'>Refresh</button>
        </div>
      )
    }
    return (
      <VictoryChart
        width={950} height={300}
        padding={50}
        containerComponent={<VictoryVoronoiContainer />}
        domainPadding={{y: 10}}
      >
        <VictoryLine
          data={this.props.graphData}
          x='time'
          y='failures'
          style={{
            data: {stroke: 'tomato', opacity: 1.0},
            labels: {fontSize: 12},
            parent: {border: '1px solid #ccc'}
          }}
        />
        <VictoryLine
          data={this.props.graphData}
          x='time'
          y='successes'
          style={{
            data: {stroke: 'darkgreen', opacity: 1.0},
            labels: {fontSize: 12},
            parent: {border: '1px solid #ccc'}
          }}
        />
      </VictoryChart>
    )
  }
})

export default AuthnAttemptsGraph
