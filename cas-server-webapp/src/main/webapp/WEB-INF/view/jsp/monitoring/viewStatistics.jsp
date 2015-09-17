    <%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp"%>

    <script type="text/javascript">
        var memory_graph, completion_graph;
        function jqueryReady() {
                head.load(
                    // C3 styles
                    //"https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.css",
                    // Bootstrap Datables CSS
                    "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css",
                    "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
//                    "https://cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css",
//                    "/cas/css/ssosessions.css"
                );

            head.load(
                "https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js",
                "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js",
                //"https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.js",
                "/cas/js/statisticsview.js",
                function() {

                    var memory = new Gauge('#memoryGauge', ${freeMemory / (totalMemory)}, {width: 200, height: 200, label: 'Memory', textClass: 'runtimeStatistics'});

                    //var uptime = new CountUp( '${startTime}', 'uptime');


                    var server_uptime = upTime('jan,01,2014,00:00:00', 'uptime');

        $('#loading, .statisticsView').toggle();

//                    memory_graph = memoryGraph();
//                    memory_graph.init('memory-chart', .40);

//                    completion_graph = memoryGraph();
//                    completion_graph.init( 'completion-chart', .85 );
            /*
                    var jvm_fd_usage = c3.generate({
                        bindto: d3.select("#jvm_fd_usage"),
                        data: {
                            columns: [
                                ['data', 65]
                            ],
                            type: 'gauge',
                        },
                        gauge: {
                            width: 15 // for adjusting arc thickness
                        },
                        color: {
                            pattern: ['#FF0000', '#F97600', '#F6C600', '#60B044'].reverse(),
                            threshold: {
                                values: [30, 60, 90, 100]
                            }
                        },
                        size: {
                            height: 120,
                            width: 90
                        }
                    });
                    var jvm_fd_usage2 = c3.generate({
                        bindto: d3.select("#jvm_fd_usage2"),
                        data: {
                            columns: [
                                ['data', 25]
                            ],
                            type: 'gauge',
                        },
                        gauge: {
                            width: 15 // for adjusting arc thickness
                        },
                        color: {
                            pattern: ['#FF0000', '#F97600', '#F6C600', '#60B044'].reverse(),
                            threshold: {
                                values: [30, 60, 90, 100]
                            }
                        },
                        size: {
                            height: 90,
                            width: 90
                        },
                        padding: {
                            top: 0,
                            right: 0,
                            bottom: 0,
                            left: 0
                        },
                        tooltip: {
                            show: false
                        }
                    });
        */
                }
            );
        };
    </script>


    <style type="text/css">
        table {
            border:0px none;
            border-collapse:collapse;
            empty-cells:show;
            background-color:#fff;
            font-size:1.1em;
            border-collapse:separate;
            border-spacing: 0px;
        }

        th {
            background:#eee;
            color:#666;
            padding:3px 5px;
            text-align:left;
            font-weight:normal;
            line-height: 24px;
        }

        td {
            padding:3px 5px;
            border-bottom:1px solid #eee;
            height:38px;
        }
    </style>

    <div id="loading">Loading...</div>

    <div class="statisticsView">
        <h1>Runtime Statistics</h1>

        <h2>Ticket Registry Statistics</h2>

        <div class="row adminPanels">
            <div class="col-lg-3 col-md-6">
                <div class="panel panel-success">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-12 text-right">
                                <div class="semi-huge">Unexpired TGTs</div>
                                <div class="huge" id="totalUsers">${unexpiredTgts}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="panel panel-success">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-12 text-right">
                                <div class="semi-huge">Unexpired STs</div>
                                <div class="huge" id="totalUsageSessions">${unexpiredSts}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="panel panel-warning">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-12 text-right">
                                <div class="semi-huge">Expired TGTs</div>
                                <div class="huge">${expiredTgts}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="panel panel-warning">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-12 text-right">
                                <div class="semi-huge">Expired STs</div>
                                <div class="huge">0</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-4">
                <h3>Performance Statistics</h3>
                <div class="well clearfix">
                    <div id="pingBox">checking...</div>
                    <%--<button id="pingBoxRefresh" type="button" class="btn btn-sm btn-info pull-right" value="statistics/ping" data-toggle="modal" data-target="#pingModal" data-remote="false">refresh</button>--%>
                    <button id="pingBoxRefresh" type="button" class="btn btn-sm btn-default pull-right" value="statistics/ping">refresh</button>
                </div>
                <%--<a href="statistics/ping?pretty=true">Ping</a>--%>
            </div>
            <div class="col-md-8">
                <h3>Thread Dump</h3>
                <div class="well clearfix">
                    <pre id="threadDumpPreview"></pre>
                    <button id="threadDumpViewButton" type="button" class="btn btn-sm btn-default pull-right" value="statistics/threads?pretty=true" data-toggle="modal" data-target="#threadDumpModal" data-remote="false">View more</button>
                </div>
            </div>
        </div>
        <%--<a href="statistics/threads?pretty=true">Thread Dump</a>--%>




        <h2>Metrics</h2>
<%--
TODO: Add metric gauges/meters here
note: should these be in an acordian/collapse?  If so, how to populate when it opens?
--%>
        <div class="row metricsPanels">
            <div class="col-md-12">
                <a href="statistics/metrics?pretty=true">Metrics</a>
                <div id="metricsGauges"></div>
            </div>
        </div>



        <h2>Reports</h2>
<%--
TODO: add SSOSessions chart here
--%>
        <div class="row performancePanels">
            <div class="col-md-4">
            <a href="statistics/ssosessions">SSO Sessions</a>
            </div>
        </div>

        <h2>Runtime Statistics</h2>
        <div class="row runtimePanels">
            <div class="col-md-4 text-center">
                <div>
<%--
TODO: add tooltip
--%>
                    <div id="memoryGauge"></div>
                    Max memory: ${maxMemory} MB
                </div>

<%--
TODO: wire up memory visualisation
--%>
<div class="progress">
  <div class="progress-bar progress-bar-warning" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 60%">
    <span class="sr-only">60% Complete (warning)</span>
  </div>
</div>

<div class="progress">
  <div class="progress-bar progress-bar-success" style="width: 35%">
    <span class="sr-only">35% Complete (success)</span>
  </div>
  <div class="progress-bar progress-bar-warning progress-bar-striped" style="width: 20%">
    <span class="sr-only">20% Complete (warning)</span>
  </div>
  <div class="progress-bar progress-bar-danger" style="width: 10%">
    <span class="sr-only">10% Complete (danger)</span>
  </div>
</div>



            </div>
            <div class="col-md-8">
<%--
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-3">
                                <i class="fa fa-server fa-5x"></i>
                            </div>
                            <div class="col-xs-9 text-right">
                                <div class="huge">${availableProcessors}</div>
                                <div>Available Processors</div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <div class="col-md-5">

            </div>
        </div>
        <div class="row runtimePanels">
            <div class="col-md-6">
--%>

                <div class="panel panel-info">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-3">
                                <i class="fa fa-clock-o fa-5x"></i>
                            </div>
                            <div class="col-xs-9 text-right">
                                <div class="huge">Uptime</div>
                                <div id="uptime"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="panel panel-info">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-3">
                                <i class="fa fa-server fa-5x"></i>
                            </div>
                            <div class="col-xs-9 text-right">
                                <div class="huge">Server Info</div>
                                <div id=""></div>
                            </div>
                        </div>
                    </div>
                    <div class="panel-body">
                        <table class="table table-striped ">
                            <thead>
                                <tr>
                                    <th class="col-md-4">Property</th>
                                    <th class="col-md-8">Value</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Server</td>
                                    <td>${serverIpAddress} (${serverHostName})</td>
                                </tr>
                                <tr>
                                    <td>CAS Ticket Suffix</td>
                                    <td>${casTicketSuffix}</td>
                                </tr>
                                <tr>
                                    <td>Server Start Time</td>
                                    <td>${startTime}</td>
                                </tr>
                                <tr>
                                    <td>Uptime</td>
                                    <td>${upTime}</td>
                                </tr>
                                <%--
                                <tr>
                                    <td>Memory</td>
                                    <td> ${freeMemory} MB free<br />
                        <p>${freeMemory} / ${totalMemory} = ${freeMemory / (totalMemory)}</p>
                        <img src="images/green.gif" width="${freeMemory / (totalMemory) * 500}" height="30" /> <br />
                        <img src="images/red.gif" width="${(totalMemory - freeMemory) / totalMemory * 500}" height="30" /> ${totalMemory} MB total
                                    </td>
                                </tr>
                                --%>
                                <tr>
                                    <td>Maximum Memory</td>
                                    <td>${maxMemory} MB</td>
                                </tr>
                                <tr>
                                    <td>Available Processors</td>
                                    <td>${availableProcessors}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

<%--
        <table width="800">
            <thead>
            <tr>
                <th>Property</th>
                <th>Value</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Server</td>
                <td>${serverIpAddress} (${serverHostName})</td>
            </tr>
            <tr>
                <td>CAS Ticket Suffix</td>
                <td>${casTicketSuffix}</td>
            </tr>
            <tr>
                <td>Server Start Time</td>
                <td>${startTime}</td>
            </tr>
            <tr>
                <td>Uptime</td>
                <td>${upTime}</td>
            </tr>
            <tr>
                <td>Memory</td>
                <td> ${freeMemory} MB free<br />
                    <p>${freeMemory} / ${totalMemory} = ${freeMemory / (totalMemory)}</p>
                    <img src="images/green.gif" width="${freeMemory / (totalMemory) * 500}" height="30" /> <br />
                    <img src="images/red.gif" width="${(totalMemory - freeMemory) / totalMemory * 500}" height="30" /> ${totalMemory} MB total
                </td>
            </tr>
            <tr>
                <td>Maximum Memory</td>
                <td>${maxMemory} MB</td>
            </tr>
            <tr>
                <td>Available Processors</td>
                <td>${availableProcessors}</td>
            </tr>
            </tbody>
        </table>
--%>
    </div>


<!-- Modal -->
        <%--
<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">×</button>
			<h3>Dialog</h3>
	</div>
	<div class="modal-body">
      <iframe src="" style="zoom:0.60" width="99.6%" height="250" frameborder="0"></iframe>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal">OK</button>
	</div>
</div>
--%>
<%--
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Modal title</h4>
      </div>
      <div class="modal-body">
        <iframe id="modalIframe" src="" style="zoom:0.60" width="99.6%" height="250" frameborder="0"></iframe>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-primary">Save changes</button>
      </div>
    </div>
  </div>
</div>
--%>
<%--
<br /><br />

<h2>Ticket Registry Statistics</h2>
<table width="800">
    <thead>
    <tr>
        <th>Property</th>
        <th>Value</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>Unexpired TGTs</td>
        <td>${unexpiredTgts}</td>
    </tr>
    <tr>
        <td>Unexpired STs</td>
        <td>${unexpiredSts}</td>
    </tr>
    <tr>
        <td>Expired TGTs</td>
        <td>${expiredTgts}</td>
    </tr>
    <tr>
        <td>Expired STs</td>
        <td>${expiredSts}</td>
    </tr>
    </tbody>
</table>
--%>


<!-- Small modal -->
<%--<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#pingModal">Small modal</button>--%>

<div id="pingModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
  <div class="modal-dialog modal-sm">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">Ping!</h4>
        </div>
        <div class="modal-body"><div class="alert alert-danger">pong would go here</div></div>
    </div>
  </div>
</div>



<div class="modal fade" id="threadDumpModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Thread Dump</h4>
      </div>
      <div class="modal-body"><pre class="thread-dump-view"></pre></div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>




<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
