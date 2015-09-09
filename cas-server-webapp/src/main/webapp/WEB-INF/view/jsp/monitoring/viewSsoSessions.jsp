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

<%--
var ssoData = {
        "activeSsoSessions":[
        {
        "authentication_date":1441736515763,
        "authentication_attributes":{
        "authenticationMethod":"HttpBasedServiceCredentialsAuthenticationHandler"
        },
        "authenticated_principal":"https://mmoayyed.unicon.net/sample1/proxyUrl",
        "number_of_uses":0,
        "ticket_granting_ticket":"PGT-2-IKlUwrZMxpkOFYifIGbcv6CYDnETaEeMdmZfRYjHdpfsndK3XI-cas01.example.org",
        "principal_attributes":{

        },
        "is_proxied":true,
        "proxied_by":"https://mmoayyed.unicon.net/sample1/",
        "authenticated_services":{

        }
        },
        {
        "authentication_date":1441736462186,
        "authentication_attributes":{
        "authenticationMethod":"AcceptUsersAuthenticationHandler"
        },
        "authenticated_principal":"casuser",
        "number_of_uses":3,
        "ticket_granting_ticket":"TGT-1-T5hpkh06BOW2tSY1cwGIMblwru6DjdndCtwpBI6oeUqnGzZepu-cas01.example.org",
        "principal_attributes":{
        "uid":"uid",
        "eduPersonAffiliation":"eduPersonAffiliation",
        "memberOf":[
        "faculty",
        "staff",
        "org"
        ],
        "groupMembership":"groupMembership"
        },
        "is_proxied":false,
        "authenticated_services":{
        "ST-3-uuFc7ddf4vEdBOIIOpN4-cas01.example.org":{
        "id":"https://mmoayyed.unicon.net/sample1/",
        "originalUrl":"https://mmoayyed.unicon.net/sample1/",
        "artifactId":null,
        "loggedOutAlready":false,
        "attributes":{

        }
        },
        "ST-1-7ZPfcD9LluG3eBZmWStj-cas01.example.org":{
        "id":"https://mmoayyed.unicon.net/sample1/",
        "originalUrl":"https://mmoayyed.unicon.net/sample1/",
        "artifactId":null,
        "loggedOutAlready":false,
        "attributes":{

        }
        },
        "ST-2-ZDheUfp2GsxrVnevhReK-cas01.example.org":{
        "id":"https://mmoayyed.unicon.net/sample1/",
        "originalUrl":"https://mmoayyed.unicon.net/sample1/",
        "artifactId":null,
        "loggedOutAlready":false,
        "attributes":{

        }
        }
        }
        }
        ]
        };
--%>

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

    function filterData( filter ) {
        if ( filter == 'no-proxy' ) {
        // Non-proxied
            console.log('filter by non-proxy');
        } else if ( filter == 'proxy' ) {
        // Proxied
            console.log('filter by proxy');
        } else {
        // All
            console.log('filter by all');
        }

/*
        $('#example').DataTable().column( 0 ).search(
            //String, Regex, SmartSearch
            '', false, false
        ).draw();
*/
    }


    function removeSession( tid ) {
        // Single Session: /statistics/ssosessions/destroySsoSession
        // Multiple Sessions: /statistics/ssosessions/destroySsoSessions

        var factory = {};
        factory.httpHeaders = {};
        factory.httpHeaders[ $("meta[name='_csrf_header']").attr("content") ] = $("meta[name='_csrf']").attr("content");

        factory.ticketId = tid;

    console.log(factory.httpHeaders);

        if ( factory.ticketId ) {
            factory.url = '/cas/statistics/ssosessions/destroySsoSession';
            // factory.data = { ticketGrantingTicket: factory.ticketId, type: 'ALL' };
        } else {
            factory.url = '/cas/statistics/ssosessions/destroySsoSessions';
        }


        $.ajax({
            type: 'post',
            url: factory.url,
            data: { ticketGrantingTicket: factory.ticketId, type: 'ALL' },
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
            "https://code.jquery.com/ui/1.11.4/themes/redmond/jquery-ui.css",
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
            "https://cdn.datatables.net/1.10.8/css/jquery.dataTables.css",
        /*
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
            "https://cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css",
        */
            "/cas/css/ssosessions.css"
        );

        head.load(
            "https://cdn.datatables.net/1.10.8/js/jquery.dataTables.min.js",
            "https://cdn.datatables.net/1.10.8/js/dataTables.jqueryui.min.js",
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js",
        /*
            "https://cdn.datatables.net/1.10.9/js/dataTables.bootstrap.min.js",
            "https://cdn.datatables.net/1.10.9/js/jquery.dataTables.min.js"
        */

            function() {

                $('#removeAllSessionsButton').on('click', function(e) {
                    e.preventDefault();
                    removeSession();
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
                $("#cas-sessions").show();
                $('#table_id-orig').DataTable();

                $('#ssoSessions').DataTable( {
                    "initComplete": function(settings, json) {
                        //console.log( 'DataTables has finished its initialisation.' );
                        $( "#ssoSessions tbody tr td:last-child button.btn-danger" ).on( "click", function() {
                            removeSession( this.value );
                        });
                        $('#loadingMessage').hide();
                    },
                    "language": {
                        //"infoEmpty": "No active sessions were found",
                        "emptyTable": "No sessions found",
                        "zeroRecords": "No matching sessions found"
                    },
                    "processing": true,
                    //data: ssoData.activeSsoSessions,
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
                                    return '';
                                }
                            }
                            //"defaultContent": ''
                        },
                        {
                            "targets": 1,
                            "data": 'authenticated_principal',
                            "render": function ( data, type, full, meta ) {
                                return type === 'display' && data.length > 20 ?
                                '<span title="'+data+'">'+data.substr( 0, 18 )+'...</span>' :
                                data;
                            }
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


            $('#filterButtons .btn').click(function() {
                //$(this).addClass('active').siblings().removeClass('active');

                var filter = $(this).data('filter');

                // Create Filter RegEx:
                if ( filter == 'proxied') {
                    var filterRegex = '^Proxy$';
                    var btnText = 'Remove Proxied Sessions';
                } else if ( filter == 'non-proxied') {
                    var filterRegex = '^$';
                    var btnText = 'Remove Non-Proxied Sessions';
                } else {
                    var filterRegex = '';
                    var btnText = 'Remove All Session';
                }

                var searchTerm = $('#ssoSessions').DataTable().column( 0 ).search(
                    //'^'+this.value+'$', true, false
                    //String, Regex, SmartSearch
                    //'^'+this.value+'$', true, false
                    filterRegex, true, false
                ).draw();

                $('#removeAllSessionsButton').text(btnText)
console.log(searchTerm.search());
                //var data = $('#ssoSessions').DataTable().search();
                //console.log( 'Search term was: ' + data );
            });



        });

        //parseJsonPayload();
    }

</script>



<div id="loadingMessage">Loading...</div>


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

                <button id="removeAllSessionsButton" class="btn btn-sm btn-danger" type="button">Remove All Sessions</button></div>

            <div id="container-stable">

                <div id="msg" style="display:none"></div>
                <table id="ssoSessions" class="display">
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
<table id="example" class="table table-striped table-bordered" cellspacing="0" width="100%">

        
        <%--

                        <table id="ssoSessions-orig" class="display">
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
        --%>
    </div>

    <div id="login">
        <input class="btn-submit" type="button" onclick="location.reload();" value="Refresh">
    </div>
</div>
</div>
</div>

<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
