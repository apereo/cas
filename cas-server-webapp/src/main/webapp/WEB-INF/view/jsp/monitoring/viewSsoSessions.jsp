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

    function parseJsonPayload() {


    }

    function showError(msg) {
        $("#msg").removeClass();
        $("#msg").addClass("errors");
        $("#msg").text(msg);
        $("#msg").show();
    }

    function showInfo(msg) {
        $("#msg").removeClass();
        $("#msg").addClass("info");
        $("#msg").text(msg);
        $("#msg").show();
    }

    function jqueryReady() {
        head.load("https://code.jquery.com/ui/1.11.4/themes/redmond/jquery-ui.css",
                "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
                "https://cdn.datatables.net/1.10.8/css/jquery.dataTables.css",
                "/cas/css/ssosessions.css");

        head.load("https://cdn.datatables.net/1.10.8/js/jquery.dataTables.min.js",
                "https://cdn.datatables.net/1.10.8/js/dataTables.jqueryui.min.js",
                function() {
                    $("#cas-sessions").show();
                    $('#table_id').DataTable();
                });

        parseJsonPayload();
    }

</script>

<div id="cas-sessions" style="display:none;">


    <!-- Main Header/Navigation
    <nav class="navbar navbar-default navbar-static-top" id="top-navbar" role="navigation">
        <div class="container">
            <span class="navbar-brand" href="#"><span class="glyphicon glyphicon-stats" aria-hidden="true"></span>
                <span class="logo hidden-xs"><span class="heavy">SSO</span>Sessions Report</span></span>
        </div>
    </nav> -->

    <div class="panel panel-default">
        <div class="panel-heading">
            <h4><span class="glyphicon glyphicon-stats" aria-hidden="true"></span> SSO Sessions Report</h4>
        </div>
        <div class="panel-body">
            <div>

                <div class="btn-group btn-group-sm pull-right" data-toggle="buttons">
                    <label class="btn btn-primary active">
                        <input type="radio" name="options" id="optionAll" autocomplete="off" checked> All
                    </label>
                    <label class="btn btn-primary">
                        <input type="radio" name="options" id="optionProxied" autocomplete="off"> Proxied
                    </label>
                    <label class="btn btn-primary">
                        <input type="radio" name="options" id="optionDirect" autocomplete="off"> Non-Proxied
                    </label>
                </div>

                <button class="btn btn-sm btn-danger" type="button">Destroy All Sessions</button></div>

            <div id="container-stable">

                <div id="msg" style="display:none"></div>

                <table id="table_id" class="display">
                    <thead>
                    <tr>
                        <th>&nbsp;</th>
                        <th>Principal</th>
                        <th>Ticket Granting Ticket</th>
                        <th>Authentication Date</th>
                        <th>Usage Count</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td></td>
                        <td>User</td>
                        <td>TGT-123456</td>
                        <td>Today</td>
                        <td>5</td>
                        <td><button class="btn btn-sm btn-danger" type="button">Destroy</button></div></span></td>
            </tr>
            <tr>
                <td><span class='label label-primary'>Proxy</span></td>
                <td>User</td>
                <td>PGT-123456</td>
                <td>Today</td>
                <td>5</td>
                <td><button class="btn btn-sm btn-danger" type="button">Destroy</button></div></span></td>

        </tr>
        </tbody>
        </table>

    </div>

    <div id="login">
        <input class="btn-submit" type="button" onclick="location.reload();" value="Refresh">
    </div>
</div>
</div>
</div>

<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
