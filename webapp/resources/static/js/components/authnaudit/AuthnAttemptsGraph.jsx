import React from 'react';
import { VictoryChart, VictoryVoronoiContainer, VictoryLine } from 'victory';

import PropTypes from 'prop-types';

const AuthnAttemptsGraph = (props) => {
  if (props.graphData.length < 2) {
    return (
      <div>
        <h4>No data found</h4>
      </div>
    );
  }

  return (
    <div>
      <VictoryChart
        width={950}
        height={300}
        padding={50}
        containerComponent={<VictoryVoronoiContainer />}
        domainPadding={{ y: 10 }}
      >
        <VictoryLine
          data={props.graphData}
          x="time"
          y="failures"
          style={{
            data: { stroke: 'tomato', opacity: 1.0 },
            labels: { fontSize: 12 },
            parent: { border: '1px solid #ccc' },
          }}
        />
        <VictoryLine
          data={props.graphData}
          x="time"
          y="successes"
          style={{
            data: { stroke: 'darkgreen', opacity: 1.0 },
            labels: { fontSize: 12 },
            parent: { border: '1px solid #ccc' },
          }}
        />
      </VictoryChart>
    </div>
  );
};

AuthnAttemptsGraph.propTypes = {
  graphData: PropTypes.arrayOf.isRequired,
};

export default AuthnAttemptsGraph;
