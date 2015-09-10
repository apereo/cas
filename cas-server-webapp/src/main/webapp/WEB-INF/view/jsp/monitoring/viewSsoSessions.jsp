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

    function updateAdminPanels( data ) {
        // Todo: wire this up
        console.log('updateAdminPanels');
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

    function removeSession( ticketId ) {
        var factory = {};
        factory.httpHeaders = {};
        factory.httpHeaders[ $("meta[name='_csrf_header']").attr("content") ] = $("meta[name='_csrf']").attr("content");

        factory.ticketId = ticketId;


        if (ticketId && (ticketId == 'ALL' || ticketId == 'PROXIED' || ticketId == 'DIRECT' ) ) {
            factory.url = '/cas/statistics/ssosessions/destroySsoSessions';
            factory.data = { type: ticketId };
        } else {
            factory.url = '/cas/statistics/ssosessions/destroySsoSession';
            factory.data = { ticketGrantingTicket: factory.ticketId };
        }


// Todo: Add filter value
console.log(factory.data);
    return;
        $.ajax({
            type: 'post',
            url: factory.url,
            //data: { ticketGrantingTicket: factory.ticketId, type: 'ALL' },
            data: factory.data,
            headers: factory.httpHeaders,
            dataType: 'json',
            success: function (data, status) {
                // Reinitialize the table data
                $('#ssoSessions').DataTable().ajax.reload();
                console.log('successfully removed ' + factory.sid);
                /*
                if(data.status != 200) {
                delayedAlert('notupdated', 'danger', data);
                } else if(angular.isString(data)) {
                sharedFactory.forceReload();
                } else {
                serviceData.getServices();
                }
                */
            },
            error: function(xhr, status) {
                console.error(xhr.responseText);
                /*
                if(xhr.status == 403) {
                sharedFactory.forceReload();
                } else {
                delayedAlert('notupdated', 'danger', xhr.responseJSON);
                }
                */
            }
        });


    }

    function jqueryReady() {
        head.load(
            // CSS Libraries
//            "https://code.jquery.com/ui/1.11.4/themes/redmond/jquery-ui.css",
//            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
//            "https://cdn.datatables.net/1.10.8/css/jquery.dataTables.css",
// Bootstrap Datables CSS
        //"https://fonts.googleapis.com/css?family=Lato",
        "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css",
        "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
        "https://cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css",

        /*
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
            "https://cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css",
        */
            "/cas/css/ssosessions.css"
        );

        head.load(
            // JS Libraries
//            "https://cdn.datatables.net/1.10.8/js/jquery.dataTables.min.js",
//            "https://cdn.datatables.net/1.10.8/js/dataTables.jqueryui.min.js",
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js",

        // Bootstrap Datatables
//        "//code.jquery.com/jquery-1.11.3.min.js",
        "https://cdn.datatables.net/1.10.9/js/jquery.dataTables.min.js",
        "https://cdn.datatables.net/1.10.9/js/dataTables.bootstrap.min.js",


        /*
            "https://cdn.datatables.net/1.10.9/js/dataTables.bootstrap.min.js",
            "https://cdn.datatables.net/1.10.9/js/jquery.dataTables.min.js"
        */

            function() {
//console.log(ssoData.activeSsoSessions);
                $('#removeAllSessionsButton').on('click', function(e) {
                    e.preventDefault();
                    removeSession(this.value);
                });

                $(document).on('click', '#filterButtons input:radio[id^="q_op_"]', function (e) {
                    //alert("click fired");
                    console.log(this.value);
                });
/*
                $('#filterButtons :radio').on('click', function(e) {
                    e.preventDefault();
                    console.log(this.value);
                    //removeSession();
                });
*/
                //$("#cas-sessions").show();
                //$('#table_id-orig').DataTable();

                $('#ssoSessions').DataTable( {
                    "order": [[ 3, "desc" ]],
                    "initComplete": function(settings, json) {

                        if (!json || json.activeSsoSessions.length == 0) {
                            console.warn('show no data view');
                            $('#loadingMessage').hide();
                            $('#no-cas-sessions').show();
                        } else {
                            //updateAdminPanels( json );

                            $( "#ssoSessions tbody tr td:last-child button.btn-danger" ).on( "click", function() {
                                removeSession( this.value );
                            });
                            $('#loadingMessage').hide();
                            $("#no-cas-sessions").hide();
                            $("#cas-sessions").show();
                        }
                    },
                    "language": {
                        //"infoEmpty": "No active sessions were found",
                        "emptyTable": "No sessions found",
                        "zeroRecords": "No matching sessions found"
                    },
                    "processing": true,
//                    data: ssoData.activeSsoSessions,

                    "ajax": {
                        //"url": '/cas/statistics/ssosessions/getSsoSessions',
                        "url": '/cas/js/test_data.json',
                        "dataSrc": "activeSsoSessions"
                    },

                    columnDefs: [
                        {
                            "targets": 0,
                            "data": 'is_proxied',
                            'className': 'col-xs-1',
                            "render" : function ( data, type, full, meta ) {
                                if ( data === true) {
                                    return '<span class="label label-primary">Proxy</span>';
                                } else {
                                    return ' ';
                                }
                            }
                        },
                        {
                            "targets": 1,
                            "data": 'authenticated_principal',
                            "className": 'col-xs-2',
                            "render": function ( data, type, full, meta ) {
                                return type === 'display' && data.length > 20 ?
                                '<span title="'+data+'">'+data.substr( 0, 18 )+'...</span>' :
                                data;
                            }
                        },
                        {
                            "targets": 2,
                            "data": 'ticket_granting_ticket',
                            "className": 'col-xs-3',
                            "render": function ( data, type, full, meta ) {
                                return type === 'display' && data.length > 20 ?
                                '<span title="'+data+'">'+data.substr( 0, 18 )+'...</span>' :
                                data;
                            }
                        },
                        {
                            "targets": 3,
                            "data": 'authentication_date',
                            "className": 'col-xs-3',
                            "render": function ( data, type, full, meta ) {
                                var timeStamp = new Date( data );
                                return timeStamp.toFormattedString();
                                //return data.toFormattedString();
                            }
                        },
                        {
                            "targets": 4,
                            "data": 'number_of_uses',
                            "className": 'col-xs-2'
                        },
                        {
                            "targets": 5,
                            "data": "ticket_granting_ticket",
                            "className": 'col-xs-1',
                            "render": function (data, type, full, meta ) {
                                return '<button class="btn btn-sm btn-danger" type="button" value="' + data + '">Destroy</button>';
                            },
                            "orderable": false
                        },
                    ]
                } );


            $('#filterButtons .btn').click(function() {

                var filter = $(this).data('filter');
                var table = $('#ssoSessions').DataTable();

                // Create Filter RegEx:
                if ( filter == 'proxied') {
                    var filterRegex = '^Proxy$';
                    var deleteValue = 'PROXIED';
                    var btnText = 'Remove <span class="badge">xx</span> Proxied Sessions';
                } else if ( filter == 'non-proxied') {
                    var filterRegex = '^ $';
                    var deleteValue = 'DIRECT';
                    var btnText = 'Remove <span class="badge">xx</span> Non-Proxied Sessions';
                } else {
                    var filterRegex = '';
                    var deleteValue = 'ALL';
                    var btnText = 'Remove All Sessions';
                }

                var searchTerm = table.column( 0 ).search(filterRegex, true, false).draw();

                $('#removeAllSessionsButton').val( deleteValue ).html(btnText.replace('xx', searchTerm.page.info().recordsDisplay ))
            });



        });

        //parseJsonPayload();
    }

</script>


<div id="loadingMessage"><h3>Loading SSO Sessions...</h3></div>

<div id="no-cas-sessions">
    <h2>No sessions found.</h2>
    <div>
        <input class="btn btn-success" type="button" onclick="location.reload();" value="Refresh">
    </div>
</div>

<div id="cas-sessions">

<%--
 Todo: Wire these messages up, auto close the alerts if its successful.
 --%>
    <div class="alert alert-success fade in" role="alert">
    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
    ...
    </div>


    <div class="panel panel-default">
        <div class="panel-heading">
            <h4><span class="glyphicon glyphicon-stats" aria-hidden="true"></span> SSO Sessions Report</h4>
        </div>
        <div class="panel-body">
            <div id="session-counts" class="container-fluid">


<div class="row adminPanels">
        <div class="col-lg-4 col-md-6">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-3">
                            <i class="fa fa-users fa-5x"></i>
                        </div>
                        <div class="col-xs-9 text-right">
                            <div class="huge">26</div>
                            <div>Total Active Users</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-4 col-md-6">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-3">
                            <i class="fa fa-tasks fa-5x"></i>
                        </div>
                        <div class="col-xs-9 text-right">
                            <div class="huge">12</div>
                            <div>Usage Count Sessions</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-4 col-md-6">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-3">
                            <i class="fa fa-ticket fa-5x"></i>
                        </div>
                        <div class="col-xs-9 text-right">
                            <div class="huge">124</div>
                            <div>Total TG Tickets</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
<%--
                <div class="row adminPanels">
                    <div class="col-md-4">
                        <div class="users-panel panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">Users</h3>
                            </div>
                            <div class="panel-body">
                                <span id="userCount">8</span>
                                <p>Total Active users</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="usage-panel panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">Usage Count</h3>
                            </div>
                            <div class="panel-body">
                                <span id="usageCount">36</span>
                                <p>Sessions</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="tickets-panel panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">Tickets</h3>
                            </div>
                            <div class="panel-body">
                                <span id="ticketCount">36</span>
                                <p>Current TGTs</p>
                            </div>
                        </div>
                    </div>
                </div>
--%>

            </div>

            <div class="container-fluid">
                <div id="filterButtons" class="btn-group btn-group-sm pull-right" data-toggle="buttons">
                    <label class="btn btn-default active" data-filter="all">
                        <input type="radio" name="options" id="optionAll" autocomplete="off"> All
                    </label>
                    <label class="btn btn-default" data-filter="proxied">
                        <input type="radio" name="options" id="optionProxied" autocomplete="off"> Proxied
                    </label>
                    <label class="btn btn-default" data-filter="non-proxied">
                        <input type="radio" name="options" id="optionDirect" autocomplete="off"> Non-Proxied
                    </label>
                </div>

                <button id="removeAllSessionsButton" class="btn btn-sm btn-danger" type="button" value="ALL">Remove All Sessions</button>
            </div>

            <div id="container-stable" class="container-fluid">

                <div id="msg" style="display:none"></div>
                <table id="ssoSessions" class="display table table-striped table-bordered">
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
    </div>

    <div id="login">
        <input class="btn-submit" type="button" onclick="location.reload();" value="Refresh">
    </div>
</div>
</div>
</div>

<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
