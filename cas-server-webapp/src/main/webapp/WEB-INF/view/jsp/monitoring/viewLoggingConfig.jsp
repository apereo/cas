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
    var logLevels = ['trace', 'debug', 'info', 'warn', 'error'];
    function jqueryReady() {
        head.load(
            // Bootstrap Datables CSS
            "https://cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css",
            "/cas/css/loggingDashboard.css"
        );

        head.load(
            // JS Libraries
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js",

            // Bootstrap Datatables
            "https://cdn.datatables.net/1.10.9/js/jquery.dataTables.min.js",
            "https://cdn.datatables.net/1.10.9/js/dataTables.bootstrap.min.js",

            "/cas/js/loggingDashboard.js"
        );
    }
</script>

<div id="loggingDashboard">
    <h1>Log Configuration Dashboard</h1>

    <div id="loadingMessage"><h3><spring:message code="cas.loggingdashboard.loading" /></h3></div>

    <div id="errorLoadingData" class="alert alert-danger hidden">
        <h2><spring:message code="cas.loggingdashboard.loading.error" /></h2>
        <div>
            <input class="btn btn-success" type="button" onclick="location.reload();" value="<spring:message code="cas.ssosessions.button.refresh" />">
        </div>
    </div>

    <div class="tabsContainer hidden row">
        <div id="alert-container"></div>
          <!-- Nav tabs -->
          <ul id="myTabs" class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#loggersTab" aria-controls="loggersTab" role="tab" data-toggle="tab">Loggers</a></li>
            <li role="presentation"><a href="#activeLoggers" aria-controls="activeLoggers" role="tab" data-toggle="tab">Active Loggers</a></li>
          </ul>

          <!-- Tab panes -->
          <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="loggersTab">
                <table id="loggersTable" class="display table table-striped table-bordered">
                    <thead>
                        <tr>
                            <th></th>
                            <th><spring:message code="cas.loggingdashboard.logger" /></th>
                            <th><spring:message code="cas.loggingdashboard.additive" /></th>
                            <th><spring:message code="cas.loggingdashboard.state" /></th>
                            <th><spring:message code="cas.loggingdashboard.level" /></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td></td>
                            <td>logger</td>
                            <td>level</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div role="tabpanel" class="tab-pane" id="activeLoggers">
                ..:: place Active loggers information here ::..
            </div>
          </div>
    </div>
</div>


<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
