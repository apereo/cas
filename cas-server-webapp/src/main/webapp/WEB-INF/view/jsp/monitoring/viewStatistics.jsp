<%@include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp"%>

    <script type="text/javascript">
        var memory_graph, completion_graph;
        function jqueryReady() {
                head.load(
                    "//maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css",
                    "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
                );

            head.load(
                "//cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js",
                "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js",
                "/cas/js/statisticsview.js",
                function() {

                    var memory = new Gauge('#memoryGauge', ${freeMemory / (totalMemory)}, {width: 200, height: 200, label: '<spring:message code="cas.statistics.section.serverstatistics.freememorygauge.label" />', textClass: 'runtimeStatistics'});
                    var maxMemory = new Gauge('#maxMemoryGauge', ${totalMemory / (maxMemory)}, {width: 200, height: 200, label: '<spring:message code="cas.statistics.section.serverstatistics.maxmemorygauge.label" />', textClass: 'runtimeStatistics'});

                    var server_uptime = upTime('${startTime}', 'uptime');

                    /**
                     * Populate the Thread Dump area.
                     * Pass it how many characters to retrieve  Default is 400
                     */
                    getThreadDumpPreview(600);

                    $('#loading, .statisticsView').toggle();
                }
            );
        };
    </script>

    <div id="loading">Loading...</div>

    <div class="statisticsView">
        <h2><spring:message code="cas.statistics.pagetitle" /></h2>

        <div class="section">
            <%-- Registry Ticket Section --%>
            <h3><spring:message code="cas.statistics.section.ticket.title" /></h3>
            <div class="row adminPanels clearfix">
                <div class="col-lg-3 col-md-6">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <div class="row">
                                <div class="col-xs-12 text-right">
                                    <div class="semi-huge"><spring:message code="cas.statistics.section.ticket.panel.unexpiredtgts.title" /></div>
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
                                    <div class="semi-huge"><spring:message code="cas.statistics.section.ticket.panel.unexpiredsts.title" /></div>
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
                                    <div class="semi-huge"><spring:message code="cas.statistics.section.ticket.panel.expiredtgts.title" /></div>
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
                                    <div class="semi-huge"><spring:message code="cas.statistics.section.ticket.panel.expiredsts.title" /></div>
                                    <div class="huge">${expiredSts}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="adminPanels container-fluid">
                <a id="viewSsoSessions" href="statistics/ssosessions" class="btn btn-primary pull-right"><spring:message code="cas.statistics.section.ticket.button" /></a>
            </div>
        </div>

            <h3><spring:message code="cas.statistics.section.serverstatistics.title" /></h3>
        <div class="row runtimePanels">
        <%-- JVM Server Section --%>
            <div class="col-md-4">
                <div class="text-center">
                    <div class="section" id="memoryUsageWrapper">
                        <div id="memoryGauge"></div>
                    </div>
                    <div id="maxMemoryWrapper">
                        <div id="maxMemoryGauge"></div>
                    </div>
                </div>
            </div>
            <div class="col-md-8">
                <div id="uptime-panel" class="panel panel-info">
                    <div class="panel-heading">
                        <div class="row">
                            <div class="col-xs-3">
                                <i class="fa fa-clock-o fa-5x"></i>
                            </div>
                            <div class="col-xs-9 text-right">
                                <div class="huge"><spring:message code="cas.statistics.section.serverstatistics.panel.uptime.title" /></div>
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
                                <div class="huge"><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.title" /></div>
                                <div id=""></div>
                            </div>
                        </div>
                    </div>
                    <div class="panel-body">
                        <table class="table table-striped ">
                            <thead>
                                <tr>
                                    <th class="col-md-4"><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.label" /></th>
                                    <th class="col-md-8"><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.value.label" /></th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.server.label" /></td>
                                    <td>${serverIpAddress} (${serverHostName})</td>
                                </tr>
                                <tr>
                                    <td><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.casticketsuffix.label" /></td>
                                    <td>${casTicketSuffix}</td>
                                </tr>
                                <tr>
                                    <td><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.starttime.label" /></td>
                                    <td>${startTime}</td>
                                </tr>
                                <tr>
                                    <td><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.uptime.label" /></td>
                                    <td>${upTime}</td>
                                </tr>
                                <tr>
                                    <td><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.memory.label" /></td>
                                    <td>${freeMemory} MB <spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.free" /> <br/>${totalMemory} MB <spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.total" /></td>
                                </tr>
                                <tr>
                                    <td><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.maxmemory.label" /></td>
                                    <td>${maxMemory} MB</td>
                                </tr>
                                <tr>
                                    <td><spring:message code="cas.statistics.section.serverstatistics.panel.serverinfo.table.property.availprocessors.label" /></td>
                                    <td>${availableProcessors}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
        <%-- Thread Dump Section --%>
            <div class="col-md-12">
                <h3><spring:message code="cas.statistics.section.threaddump.title" /></h3>
                <div class="well clearfix">
                    <pre id="threadDumpPreview"></pre>
                    <button id="threadDumpViewButton" type="button" class="btn btn-sm btn-default pull-right" value="statistics/threads?pretty=true" data-toggle="modal" data-target="#threadDumpModal" data-remote="false"><spring:message code="cas.statistics.section.threaddump.button" /></button>
                </div>
            </div>
        </div>

        <div class="row">
        <%-- Metrics Section --%>
            <div class="col-sm-12">
                <h3><spring:message code="cas.statistics.section.metrics.title" /></h3>
                <a href="statistics/metrics?pretty=true" class="btn btn-default"><spring:message code="cas.statistics.section.metrics.button" /></a>
            </div>
        </div>
    </div>

<div class="modal fade" id="threadDumpModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel"><spring:message code="cas.statistics.section.threaddump.modal.title" /></h4>
      </div>
      <div class="modal-body"><pre class="thread-dump-view"></pre></div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code="cas.statistics.modal.close.button" /></button>
      </div>
    </div>
  </div>
</div>




<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
