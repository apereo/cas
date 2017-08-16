import React from 'react';
import ReactDOM from 'react-dom';
import axios from 'axios';
import moment from 'moment';
import AuthnAttemptsGraph from './components/authnaudit/AuthnAttemptsGraph';

const refreshButtonStyle = {
  position: 'absolute',
  top: '0',
  right: '15px',
};

class StatisticsApp extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      start: '',
      graphData: [],
      refreshing: false,
      scale: 'PT01M',
      range: 'PT03H',
    };

    this.getData = this.getData.bind(this);
    this.refreshData = this.refreshData.bind(this);
  }

  refreshData(e) {
    this.setState({ refreshing: true });
    this.getData();
  }

  getData() {
    const d = new Date();
    const startTime = d.setHours(d.getHours() - 2);
    axios.get(`/cas/status/stats/getAuthnAudit/summary?start=${startTime}&range=${this.state.range}&scale=${this.state.scale}`)
      .then((res) => {
        res.data.forEach((value) => {
          value.time = moment(value.time).utc().format('h:mm');
        });
        const graphData = res.data;
        this.setState({ graphData, refreshing: false });
      });
  }

  componentDidMount() {
    this.getData();
  }

  render() {
    const refreshIcon = {
      fa: true,
      'fa-refresh': true,
      'fa-spin': this.state.refreshing,
    };

    let button = null;

    if (this.state.graphData.length < 2) {
      button = (<button className="btn btn-primary btn-sm" disabled={this.state.refreshing} onClick={this.refreshData}><i
        className={refreshIcon}
      /> Refresh</button>);
    } else {
      button = (<button
        style={refreshButtonStyle}
        className="btn btn-default btn-xs"
        disabled={this.state.refreshing}
        onClick={this.refreshData}
      ><i className={refreshIcon} /> Refresh</button>);
    }

    return (
      <div>
        <h3>Authentication Attempts</h3>
        <AuthnAttemptsGraph graphData={this.state.graphData} />
        {button}
      </div>
    );
  }
}

ReactDOM.render(
  <StatisticsApp />,
  document.getElementById('statistics-app'),
);
