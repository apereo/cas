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

                    var memory = new Gauge('#memoryGauge', ${freeMemory / (totalMemory)}, {width: 200, height: 200, label: 'Total JVM Memory', textClass: 'runtimeStatistics'});
                    var maxMemory = new Gauge('#maxMemoryGauge', ${totalMemory / (maxMemory)}, {width: 200, height: 200, label: 'Max Memory', textClass: 'runtimeStatistics'});

                    var server_uptime = upTime('${startTime}', 'uptime');

                    $('#loading, .statisticsView').toggle();
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

        <div class="row adminPanels clearfix">
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
        <a href="statistics/ssosessions" class="btn btn-primary pull-right ">View SSO Sessions</a>

        <br />

        <h2>JVM Server Statistics</h2>
        <div class="row runtimePanels">
            <div class="col-md-4">
                <div class="text-center">
                    <div id="memoryUsageWrapper">
                        <%--
                        TODO: add tooltip
                        --%>
                        <div id="memoryGauge"></div>
                        <%--<h4>JVM Memory</h4>--%>
                        <%--Max memory: ${maxMemory} MB--%>
                    </div>
                    <div id="maxMemoryWrapper">
                        <%--
                        TODO: add tooltip
                        --%>
                        <div id="maxMemoryGauge"></div>
                        <%--<h4>Max Memory</h4>--%>
                        <%--Max memory: ${maxMemory} MB--%>
                    </div>
                </div>
            </div>
            <div class="col-md-8">
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
                                <tr>
                                    <td>Memory</td>
                                    <td>${freeMemory} MB free <br/>${totalMemory} MB total</td>
<%--
                                    <td>
                                        <c:set var="freeMem" value="${fn:substringBefore(freeMemory / (totalMemory) * 100, '.')}"/>
                                        <c:set var="maxMem" value="${fn:substringBefore((totalMemory - freeMemory) / totalMemory * 100, '.')}"/>

                                        <div class="progress">
                                          <div class="progress-bar" role="progressbar" aria-valuenow="${freeMem}" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em;width: ${freeMem}%;">
                                                ${freeMem}%
                                          </div>
                                        </div>

                                        <div class="progress">
                                          <div class="progress-bar progress-bar-danger" role="progressbar" aria-valuenow="maxMem" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em;width: ${maxMem}%;">
                                            ${maxMem}%
                                          </div>
                                        </div>

                                        ${freeMemory} MB free <br> <img src="images/green.gif" width="${freeMemory / (totalMemory) * 500}" height="30" /><img src="images/red.gif" width="${(totalMemory - freeMemory) / totalMemory * 500}" height="30" /> ${totalMemory} MB total
                                    </td>
--%>
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
                    </div>
                </div>
            </div>
        </div>



        <div class="row">
            <%--<div class="col-md-4">--%>
<%--
                <h3>SSO Sessions</h3>
                <table class="table">
                    <tbody>
                        <tr>
                            <td>Total Sessions</td>
                            <td>4</td>
                        </tr>
                        <tr>
                            <td>Proxied Sessions</td>
                            <td>4</td>
                        </tr>
                        <tr>
                            <td>Direct Sessions</td>
                            <td>4</td>
                        </tr>
                        <tr>
                            <td>Total Sessions</td>
                            <td>4</td>
                        </tr>
                    </tbody>
                </table>
                <a href="statistics/ssosessions" class="btn btn-default">View SSO Sessions</a>
--%>
<%--
                <h3>Performance Statistics</h3>
                <div class="well clearfix">
                    <div id="pingBox">checking...</div>
                    <button id="pingBoxRefresh" type="button" class="btn btn-sm btn-default pull-right" value="statistics/ping">refresh</button>
                </div>
--%>
            <%--</div>--%>
            <div class="col-md-12">
                <h3>Thread Dump</h3>
                <div class="well clearfix">
                    <pre id="threadDumpPreview"></pre>
                    <button id="threadDumpViewButton" type="button" class="btn btn-sm btn-default pull-right" value="statistics/threads?pretty=true" data-toggle="modal" data-target="#threadDumpModal" data-remote="false">View more</button>
                </div>
            </div>
        </div>



        <h2>Metrics</h2>
        <div class="row">
            <div class="col-sm-12">
                <a href="statistics/metrics?pretty=true" class="btn btn-default">Metrics</a>
            </div>
<%--
            <div class="col-md-6 col-sm-12">
                <h2>Gauges</h2>
                <div class="row metricsPanels">
                    <div class="col-md-12">
                        <a href="statistics/metrics?pretty=true">Metrics</a>
                        <table class="table">
                            <thead>
                                <tr>
                                    <th class="col-sm-4">Property</th>
                                    <th class="col-sm-8">Value</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>jvm.fd.usage</td>
                                    <td>0.0087890625</td>
                                </tr>
                            </tbody>
                        </table>
                        <div id="metricsGauges">
                        </div>
                    </div>
                </div>
            </div>
--%>
        </div>

<%--
TODO: Add metric gauges/meters here
note: should these be in an acordian/collapse?  If so, how to populate when it opens?
--%>


<%--
        <h2>Reports</h2>
        <div class="row performancePanels">
            <div class="col-md-4">
            <a href="statistics/ssosessions">SSO Sessions</a>
            </div>
        </div>
--%>

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

<!-- Small modal -->
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
