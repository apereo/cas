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
    /*
     * End Points:
    /statistics/ssosessions/getSsoSessions [GET] renders JSON blob that gets you all session data

    /statistics/ssosessions/destroySsoSession [POST] destroys a single SSO session. Requires a TGT as a parameter available in the JSON blob

    /statistics/ssosessions/destroySsoSessions [POST] bulk kill. Destroys all. Optional parameters can be "ALL", "PROXIED", "DIRECT"
     */

    String.prototype.padLeft = function (length, character) {
        return new Array(length - this.length + 1).join(character || ' ') + this;
    };

    Date.prototype.toFormattedString = function () {
        return [String(this.getMonth()+1).padLeft(2, '0'),
        String(this.getDate()).padLeft(2, '0'),
        String(this.getFullYear()).substr(2, 2)].join("/") + " " +
        [String(this.getHours()).padLeft(2, '0'),
        String(this.getMinutes()).padLeft(2, '0')].join(":");
    };


    function parseJsonPayload() {}

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

    function killSession( sid ) {
        if ( sid ) {
            console.warn('kill session - ', sid);
        } else {
            console.warn('kill all sessions');
        }
    }

    function jqueryReady() {
        head.load(
            "https://code.jquery.com/ui/1.11.4/themes/redmond/jquery-ui.css",
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
            "https://cdn.datatables.net/1.10.8/css/jquery.dataTables.css",
            //"//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
            //"//cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css",
            "/cas/css/ssosessions.css"
        );

        head.load(
            "https://cdn.datatables.net/1.10.8/js/jquery.dataTables.min.js",
            "https://cdn.datatables.net/1.10.8/js/dataTables.jqueryui.min.js",

            function() {

                $('#killAllSessionsButton').on('click', function(e) {
                    e.preventDefault();
                    killSession();
                });

                $("#cas-sessions").show();
                $('#table_id-orig').DataTable();

                $('#table_id').DataTable( {
                    "initComplete": function(settings, json) {
                        //console.log( 'DataTables has finished its initialisation.' );
                        $( "#table_id tbody tr td:last-child button.btn-danger" ).on( "click", function() {
                            killSession( this.value );
                        });
                    },
                    "processing": true,
                    "ajax": {
                        "url": '/cas/statistics/ssosessions/getSsoSessions',
                        "dataSrc": "activeSsoSessions"
                    },
                    columnDefs: [
                        {
                            "targets": 0,
                            "data": 'is_proxied',
                            "render" : function ( data, type, full, meta ) {
                                if ( data === true) {
                                    return '<span class="label label-primary">Proxy</span>';
                                } else {
                                    return '&nbsp;';
                                }
                            }
                            //"defaultContent": ''
                        },
                        {
                            "targets": 1,
                            "data": 'authenticated_principal'
                        },
                        {
                            "targets": 2,
                            "data": 'ticket_granting_ticket',
                            "render": function ( data, type, full, meta ) {
                                return type === 'display' && data.length > 20 ?
                                '<span title="'+data+'">'+data.substr( 0, 18 )+'...</span>' :
                                data;
                            }
                        },
                        {
                            "targets": 3,
                            "data": 'authentication_date',
                            "render": function ( data, type, full, meta ) {
                                var timeStamp = new Date( data );
                                return timeStamp.toFormattedString();
                                //return data.toFormattedString();
                            }
                        },
                        {
                            "targets": 4,
                            "data": 'number_of_uses'
                        },
                        {
                            "targets": 5,
                            "data": "ticket_granting_ticket",
                            "render": function (data, type, full, meta ) {
                                return '<button class="btn btn-sm btn-danger" type="button" value="' + data + '">Destroy</button>';
                            }
                        },
                    ]
/*
                    "columns": [
                        { data: ''},
                        { data: "authenticated_principal" }
                    ]
*/
                } );

            });

        //parseJsonPayload();
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
                    <label class="btn btn-default">
                        <input type="radio" name="options" id="optionProxied" autocomplete="off"> Proxied
                    </label>
                    <label class="btn btn-default">
                        <input type="radio" name="options" id="optionDirect" autocomplete="off"> Non-Proxied
                    </label>
                </div>

                <button id="killAllSessionsButton" class="btn btn-sm btn-danger" type="button">Destroy All Sessions</button></div>

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
                            <td>TGT</td>
                            <td>Auth_Date</td>
                            <td>Usage_Count</td>
                            <td></td>
                        </tr>
                    </tbody>
                </table>
<hr />
                <table id="table_id-orig" class="display">
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
