/*eslint-disable no-unused-vars*/
String.prototype.padLeft = function (length, character) {
    return new Array(length - this.length + 1).join(character || ' ') + this;
};

Date.prototype.toFormattedString = function () {
    return [String(this.getMonth() + 1).padLeft(2, '0'),
        String(this.getDate()).padLeft(2, '0'),
        String(this.getFullYear()).substr(2, 2)].join('/') + ' ' +
          [String(this.getHours()).padLeft(2, '0'),
              String(this.getMinutes()).padLeft(2, '0')].join(':');
};

function principalAttributes(obj) {
    var output = '<table class="table table-condensed principal_attributes"><tbody>';
    for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
            if (Array.isArray(obj[key])) {
                output = output.concat('<tr><td class="field-label active">' + key + '</td><td>' + obj[key].toString() + '</td></tr>');
            } else {
                output = output.concat('<tr><td class="field-label active">' + key + '</td><td>' + obj[key] + '</td></tr>');
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
            for (var foo in obj[key]) {
                if (obj[key].hasOwnProperty(foo)) {
                    if (Array.isArray(obj[key][foo])) {
                        output = output.concat('<tr><td class="field-label active">' + foo + ':</td><td>' + obj[key][foo].toString() + '</td></tr>');
                    } else {
                        output = output.concat('<tr><td class="field-label active">' + foo + ':</td><td>' + obj[key][foo] + '</td></tr>');
                    }
                }
            }
            output = output.concat('</tbody></table>');
        }
    }
    return output;
}

/* Formatting function for row details - modify as you need */
function format(d) {
    return '<table class="table table-bordered row-detail">' +
      '<tbody>' +
      '<tr class="hidden-md hidden-lg">' +
      '<td class="field-label active">Access Date:</td>' +
      '<td>' + d.authentication_date_formatted + '</td>' +
      '</tr>' +
      '<tr class="hidden-md hidden-lg">' +
      '<td class="field-label active">Usage Count:</td>' +
      '<td>' + d.number_of_uses + '</td>' +
      '</tr>' +
      '<tr>' +
      '<td class="field-label active">Ticket Granting Ticket:</td>' +
      '<td>' + d.ticket_granting_ticket + '</td>' +
      '</tr>' +
      '<tr>' +
      '<td class="field-label active">Principal Attributes:</td>' +
      '<td>' +
      principalAttributes(d.principal_attributes) +
      '</td>' +
      '</tr>' +
      '<tr>' +
      '<td class="field-label active">Authenticated Services:</td>' +
      '<td>' +
      authenticatedServices(d.authenticated_services) +
      '</td>' +
      '</tr>' +
      '<tr>' +
      '<td class="field-label active">Ticket Granting Service:</td>' +
      '<td></td>' +
      '</tr>' +
      '</tbody></table>';

}

function updateAdminPanels(data) {
    //$('#totalUsers').text(data.totalPrincipals);
    $('#totalUsers').text(data.activeSsoSessions.length);
    $('#totalUsageSessions').text(sum(data.activeSsoSessions, 'number_of_uses'));
    //$('#totalProxied').text(data.totalTicketGrantingTickets);
    $('#totalTGTs').text(data.totalTicketGrantingTickets);
    //$('#totalTGTs').text( sum(data.activeSsoSessions, 'is_proxied' ) );
}

function sum(obj, prop) {
    var sum = 0;
    for (var el in obj) {
        if (obj.hasOwnProperty(el)) {
            sum += ( typeof obj[el][prop] == 'boolean' ) ? +obj[el][prop] : obj[el][prop];
        }
    }
    return sum;
}

/*
 function showError(msg) {
 $('#msg').removeClass();
 $('#msg').addClass('errors');
 $('#msg').text(msg);
 $('#msg').show();
 }
 */

/*
 function showInfo(msg) {
 $('#msg').removeClass();
 $('#msg').addClass('info');
 $('#msg').text(msg);
 $('#msg').show();
 }
 */

function alertUser(message, alertType) {
    $('#alertWrapper').append('<div id="alertdiv" class="alert alert-' + alertType + ' alert-dismissible">' +
      '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
      '<span class="alertMessage">' + message + '</span></div>'
    );

    setTimeout(function () { // this will automatically close the alert and remove this if the users doesnt close it in 5 secs
        $('#alertdiv').remove();
    }, 5000);
}

function removeSession(ticketId) {
    var factory = {};
    factory.httpHeaders = {};
    factory.messages = {};
    factory.httpHeaders[$('meta[name=\'_csrf_header\']').attr('content')] = $('meta[name=\'_csrf\']').attr('content');

    factory.ticketId = ticketId;


    if (ticketId && (ticketId == 'ALL' || ticketId == 'PROXIED' || ticketId == 'DIRECT' )) {
        factory.url = urls.destroy.all;
        factory.data = {type: ticketId};
        factory.messages.success = 'Removed <strong>' + ticketId + '</strong> tickets successfully.';
        factory.messages.error = 'Could not remove <strong>' + ticketId + '</strong> tickets.';
    } else {
        factory.url = urls.destroy.single;
        factory.data = {ticketGrantingTicket: factory.ticketId};
        factory.messages.success = 'Ticket is removed successfully.';
        factory.messages.error = 'Ticket is not removed successfully.';
    }

    $.ajax({
        type: 'post',
        url: factory.url,
        //data: { ticketGrantingTicket: factory.ticketId, type: 'ALL' },
        data: factory.data,
        headers: factory.httpHeaders,
        dataType: 'json',
        success: function (data) {
            // Reinitialize the table data
            $('#ssoSessions').DataTable().ajax.reload();

            if (data.status != 200) {
                alertUser(factory.messages.error, 'danger');
            } else {
                alertUser(factory.messages.success, 'success');
                // Reload the page
                location.reload();
            }
        },
        error: function () {
            alertUser('There appears to be an error. Please try your request again.', 'danger');
        }
    });
}

var ssoSessions = (function () {
    var createDataTable = function () {
        $('#ssoSessions').DataTable({
            'order': [[3, 'desc']],
            'initComplete': function (settings, json) {
                if (!json || json.activeSsoSessions.length == 0) {
                    $('#loadingMessage').hide();
                    $('#no-cas-sessions').show();
                } else {
                    updateAdminPanels(json);

                    $('#loadingMessage').hide();
                    $('#no-cas-sessions').hide();
                    $('#cas-sessions').show();
                }
            },
            'language': {
                //"infoEmpty": "No active sessions were found",
                'emptyTable': 'No sessions found',
                'zeroRecords': 'No matching sessions found'
            },
            'processing': true,
            'ajax': {
                'url': urls.getSessions,
                'dataSrc': 'activeSsoSessions'
            },

            columnDefs: [
                {
                    'targets': 0,
                    'className': 'details-control',
                    'orderable': false,
                    'data': null,
                    'defaultContent': ''
                },
                {
                    'targets': 1,
                    'data': 'is_proxied',
                    'className': 'col-xs-2 col-md-1',
                    'render': function (data) {
                        if (data === true) {
                            return '<span class="label label-primary">Proxy</span>';
                        } else {
                            return ' ';
                        }
                    }
                },
                {
                    'targets': 2,
                    'data': 'authenticated_principal',
                    'className': 'col-xs-4 col-md-2',
                    'render': function (data, type) {
                        return type === 'display' && data.length > 20 ?
                            '<span title="' + data + '">' + data.substr(0, 18) + '...</span>' :
                            data;
                    }
                },
                {
                    'targets': 3,
                    'data': 'ticket_granting_ticket',
                    'className': 'hidden-xs hidden-sm col-md-4',
                    'render': function (data, type) {
                        return type === 'display' && data.length > 20 ?
                            '<span title="' + data + '">' + data.substr(0, 40) + '...</span>' :
                            data;
                    }
                },
                {
                    'targets': 4,
                    'data': 'authentication_date_formatted',
                    'className': 'col-xs-4 col-sm-4 col-md-2'
                },
                {
                    'targets': 5,
                    'data': 'number_of_uses',
                    'className': 'hidden-xs hidden-sm visible-md-* col-md-2'
                },
                {
                    'targets': 6,
                    'data': 'ticket_granting_ticket',
                    'className': 'col-xs-2 col-sm-2 col-md-1',
                    'render': function (data) {
                        return '<button class="btn btn-xs btn-block btn-danger" type="button" value="' + data + '">Destroy</button>';
                    },
                    'orderable': false
                },
            ]
        });
    };

    var addEventHandlers = function () {

        /**
         * The Bulk remove button
         */
        $('#removeAllSessionsButton').on('click', function (e) {
            e.preventDefault();
            removeSession(this.value);
        });

        /**
         * Individual removal button
         */
        $(document).on('click', '#ssoSessions tbody tr td:last-child button.btn-danger', function (e) {
            e.preventDefault();
            removeSession(this.value);
        });

        /**
         * The filter buttons
         */
        $('#filterButtons .btn').click(function () {

            var filter = $(this).data('filter');
            var table = $('#ssoSessions').DataTable();
            var filterRegex;
            var deleteValue;
            var btnText;

            // Create Filter RegEx:
            if (filter == 'proxied') {
                filterRegex = '^Proxy$';
                deleteValue = 'PROXIED';
                btnText = 'Remove <span class="badge">xx</span> Proxied Sessions';
            } else if (filter == 'non-proxied') {
                filterRegex = '^ $';
                deleteValue = 'DIRECT';
                btnText = 'Remove <span class="badge">xx</span> Non-Proxied Sessions';
            } else {
                filterRegex = '';
                deleteValue = 'ALL';
                btnText = 'Remove All Sessions';
            }

            var searchTerm = table.column(1).search(filterRegex, true, false).draw();

            $('#removeAllSessionsButton').val(deleteValue).html(btnText.replace('xx', searchTerm.page.info().recordsDisplay));
        });


        // Add event listener for opening and closing details
        $(document).on('click', '#ssoSessions tbody td.details-control', function () {
            var table = $('#ssoSessions').DataTable();
            var tr = $(this).closest('tr');
            var row = table.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            }
            else {
                // Open this row
                row.child(format(row.data()), 'info').show();
                tr.addClass('shown');
            }
        });
    };

    // initialization *******
    (function init() {
        addEventHandlers();
        createDataTable();
    })();

    // Public Methods
    return {
        /**
         * Not used
         */
    };
})();
