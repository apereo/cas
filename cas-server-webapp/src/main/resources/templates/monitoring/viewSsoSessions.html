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

        function principalAttributes(obj) {
            var output = '<table class="table table-condensed principal_attributes"><tbody>';
            for (var key in obj) {
                if (obj.hasOwnProperty(key)) {
                    if (Array.isArray(obj[key])) {
                        output = output.concat('<tr><td class="field-label active">'+ key +'</td><td>' + obj[key].toString() + '</td></tr>');
                    } else {
                        output = output.concat('<tr><td class="field-label active">'+ key +'</td><td>' + obj[key] + '</td></tr>');
                    }
                }
            }
            output = output.concat('</tbody></table>');

            return output;
        }

        function authenticatedServices(obj) {
            var output = '';
            for (var key in obj) {
                if (obj.hasOwnProperty(key)) {
                    output = output.concat('<h5>' + key + '</h5><table class="table table-condensed principal_attributes"><tbody>');
                    for (foo in obj[key] ) {
                        if (obj[key].hasOwnProperty(foo)) {
                            if (Array.isArray(obj[key][foo])) {
                                output = output.concat('<tr><td class="field-label active">'+ foo +':</td><td>' + obj[key][foo].toString() + '</td></tr>');
                            } else {
                                output = output.concat('<tr><td class="field-label active">'+ foo +':</td><td>' + obj[key][foo] + '</td></tr>');
                            }
                        }
                    }
                    output = output.concat('</tbody></table>');
                }
            }
            return output;
        }

        /* Formatting function for row details - modify as you need */
        function format ( d ) {
            return '<table class="table table-bordered row-detail">' +
                '<tbody>'+
                '<tr class="hidden-md hidden-lg">'+
                    '<td class="field-label active">Access Date:</td>'+
                    '<td>'+d.authentication_date_formatted+'</td>'+
                '</tr>'+
                '<tr class="hidden-md hidden-lg">'+
                    '<td class="field-label active">Usage Count:</td>'+
                    '<td>'+d.number_of_uses+'</td>'+
                '</tr>'+
                '<tr>'+
                    '<td class="field-label active">Ticket Granting Ticket:</td>'+
                    '<td>'+d.ticket_granting_ticket+'</td>'+
                '</tr>'+
                '<tr>'+
                    '<td class="field-label active">Principal Attributes:</td>'+
                    '<td>' +
principalAttributes(d.principal_attributes) +
                    '</td>' +
                '</tr>'+
                '<tr>'+
                    '<td class="field-label active">Authenticated Services:</td>'+
                    '<td>' +
                        authenticatedServices(d.authenticated_services);
                    '</td>' +
                '</tr>'+
                '<tr>'+
                    '<td class="field-label active">Ticket Granting Service:</td>'+
                    '<td></td>'+
                '</tr>'+
            '</tbody></table>';

        }

        function updateAdminPanels( data ) {
            //$('#totalUsers').text(data.totalPrincipals);
            $('#totalUsers').text(data.activeSsoSessions.length);
            $('#totalUsageSessions').text( sum(data.activeSsoSessions, 'number_of_uses') );
            //$('#totalProxied').text(data.totalTicketGrantingTickets);
            $('#totalTGTs').text(data.totalTicketGrantingTickets);
            //$('#totalTGTs').text( sum(data.activeSsoSessions, 'is_proxied' ) );
        }

        function sum( obj, prop ) {
            var sum = 0;
            for( var el in obj ) {
                if( obj.hasOwnProperty( el ) ) {
                    sum += ( typeof obj[el][prop] == 'boolean' ) ? +obj[el][prop] : obj[el][prop] ;
                }
            }
            return sum;
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

        function alertUser(message, alertType) {
            $('#alertWrapper').append('<div id="alertdiv" class="alert alert-' +  alertType + ' alert-dismissible">' +
                '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                '<span class="alertMessage">' + message + '</span></div>'
            );

            setTimeout(function() { // this will automatically close the alert and remove this if the users doesnt close it in 5 secs
                $("#alertdiv").remove();
            }, 5000);
        }

        function removeSession( ticketId ) {
            var factory = {};
            factory.httpHeaders = {};
            factory.messages = {};
            factory.httpHeaders[ $("meta[name='_csrf_header']").attr("content") ] = $("meta[name='_csrf']").attr("content");

            factory.ticketId = ticketId;


            if (ticketId && (ticketId == 'ALL' || ticketId == 'PROXIED' || ticketId == 'DIRECT' ) ) {
                factory.url = '/cas/statistics/ssosessions/destroySsoSessions';
                factory.data = { type: ticketId };
                factory.messages.success = '<spring:message code="cas.sessions.alert.removal.success.multi.partone" /> ' + ticketId + ' <spring:message code="cas.sessions.alert.removal.success.multi.parttwo" />';
                factory.messages.error = '<spring:message code="cas.sessions.alert.removal.error.multi.partone" /> ' + ticketId + ' <spring:message code="cas.sessions.alert.removal.error.multi.parttwo" />.';
            } else {
                factory.url = '/cas/statistics/ssosessions/destroySsoSession';
                factory.data = { ticketGrantingTicket: factory.ticketId };
                factory.messages.success = '<spring:message code="cas.sessions.alert.removal.success.single.partone" /> ' + ticketId + ' <spring:message code="cas.sessions.alert.removal.success.single.parttwo" />';
                factory.messages.error = '<spring:message code="cas.sessions.alert.removal.error.single.partone" /> ' + ticketId + ' <spring:message code="cas.sessions.alert.removal.error.single.parttwo" />.';
            }

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



                    if ( data.status != 200 ) {
                        alertUser(factory.messages.error, 'danger');
                    } else {
                        alertUser( factory.messages.success, 'success' );
                        // Reload the page
                        location.reload();
                    }
                },
                error: function(xhr, status) {
                    alertUser('There appears to be an error. Please try your request again.', 'danger');
                }
            });
        }

        function jqueryReady() {
            head.load(
                // Bootstrap Datables CSS
                "https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css",
                "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css",
                "https://cdn.datatables.net/1.10.9/css/dataTables.bootstrap.min.css",
                "/cas/css/ssosessions.css"
            );

            head.load(
                // JS Libraries
                "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js",

                // Bootstrap Datatables
                "https://cdn.datatables.net/1.10.9/js/jquery.dataTables.min.js",
                "https://cdn.datatables.net/1.10.9/js/dataTables.bootstrap.min.js",

                "/cas/js/ssosessions.js"
            );
        }

    </script>

<div class="ssoSessions">
    <div id="loadingMessage"><h3><spring:message code="cas.ssosessions.loading" /></h3></div>

    <div id="no-cas-sessions">
        <h2><spring:message code="cas.ssosessions.nosessionsfound" /></h2>
        <div>
            <input class="btn btn-success" type="button" onclick="location.reload();" value="<spring:message code="cas.ssosessions.button.refresh" />">
        </div>
    </div>

    <div id="cas-sessions">
        <div id="alertWrapper"></div>

        <div class="panel panel-default">
            <div class="panel-heading">
                <h4><span class="glyphicon glyphicon-stats" aria-hidden="true"></span> <spring:message code="cas.ssosessions.report.pagetitle" /></h4>
            </div>
            <div class="panel-body">
                <div id="session-counts" class="container-fluid">
                    <div class="row adminPanels">
                        <div class="col-lg-3 col-md-6">
                            <div class="panel panel-info">
                                <div class="panel-heading">
                                    <div class="row">
                                        <div class="col-xs-3">
                                            <i class="fa fa-users fa-5x"></i>
                                        </div>
                                        <div class="col-xs-9 text-right">
                                            <div class="huge" id="totalUsers">0</div>
                                            <div><spring:message code="cas.ssosessions.report.panel.totalactiveprincipals" /></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-3 col-md-6">
                            <div class="panel panel-success">
                                <div class="panel-heading">
                                    <div class="row">
                                        <div class="col-xs-3">
                                            <i class="fa fa-tasks fa-5x"></i>
                                        </div>
                                        <div class="col-xs-9 text-right">
                                            <div class="huge" id="totalUsageSessions">0</div>
                                            <div><spring:message code="cas.ssosessions.report.panel.usagecountsessions" /></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-3 col-md-6">
                            <div class="panel panel-warning">
                                <div class="panel-heading">
                                    <div class="row">
                                        <div class="col-xs-3">
                                            <i class="fa fa-ticket fa-5x"></i>
                                        </div>
                                        <div class="col-xs-9 text-right">
                                            <div class="huge" id="totalTGTs">0</div>
                                            <div><spring:message code="cas.ssosessions.report.panel.totalssosessions" /></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <%--<div class="col-md-12">graph</div>--%>
                    </div>
                </div>

                <div class="container-fluid">
                    <div id="filterButtons" class="btn-group btn-group-sm pull-right" data-toggle="buttons">
                        <label class="btn btn-default active" data-filter="all">
                            <input type="radio" name="options" id="optionAll" autocomplete="off"> <spring:message code="cas.ssosessions.buttons.filter.all" />
                        </label>
                        <label class="btn btn-default" data-filter="proxied">
                            <input type="radio" name="options" id="optionProxied" autocomplete="off"> <spring:message code="cas.ssosessions.buttons.filter.proxied" />
                        </label>
                        <label class="btn btn-default" data-filter="non-proxied">
                            <input type="radio" name="options" id="optionDirect" autocomplete="off"> <spring:message code="cas.ssosessions.buttons.filter.nonproxied" />
                        </label>
                    </div>

                    <button id="removeAllSessionsButton" class="btn btn-sm btn-danger" type="button" value="ALL"><spring:message code="cas.ssosessions.buttons.removeall" /></button>
                </div>

                <div id="container-stable" class="container-fluid">

                    <div id="msg" style="display:none"></div>
                    <table id="ssoSessions" class="display table table-striped table-bordered">
                        <thead>
                            <tr>
                                <th>&nbsp;</th>
                                <th>&nbsp;</th>
                                <th><spring:message code="cas.ssosessions.table.header.principal" /></th>
                                <th><spring:message code="cas.ssosessions.table.header.ticketgrantingticket" /></th>
                                <th><spring:message code="cas.ssosessions.table.header.authenticationdate" /></th>
                                <th><spring:message code="cas.ssosessions.table.header.usagecount" /></th>
                                <th>&nbsp;</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td></td>
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
                    <input class="btn-submit" type="button" onclick="location.reload();" value="<spring:message code="cas.ssosessions.button.refresh" />">
                </div>
            </div>
        </div>
    </div>
</div>
<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
