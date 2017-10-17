var infoEl = $('tr.info').clone();
var strings = strings;

/* Formatting function for row details - modify as you need */
function format(d) {
    var detail = infoEl.clone();
    // show
    $(detail).toggleClass('hidden');
    // add data
    var dec = d.decision;
    // date
    detail.find('.created-date')[0].append(date(dec.createdDate).toLocaleString());
    // options & reminder
    if (dec.options === 'ALWAYS') {
        detail.find('.consent-reminder').parent().remove();
    } else {
        var unit = dec.reminderTimeUnit.toLowerCase();
        detail.find('.consent-reminder span:not(.' + unit + ')').remove();
        if (dec.reminder === 1) {
            var _unit = detail.find('.consent-reminder span.' + unit);
            _unit.html(_unit.text().slice(0, -1));
        }
        detail.find('.consent-reminder').prepend(dec.reminder);
    }
    detail.find('.consent-options span:not(.' + dec.options.toLowerCase().replace('_','-') + ')').remove();
    // render attribute table
    attributeTable(detail.find('.consent-attributes'),d.attributes);
    // enable tooltip
    detail.find('.consent-options [data-toggle="tooltip"]').tooltip();
    // setup delete button
    var del = detail.find('.btn-danger');
    var data = { 'id': dec.id, 'service': dec.service };
    del.on('click', data, function (e) {
        e.preventDefault();
        confirm(e.data.id, e.data.service);
    });
        
    return detail;
}

function alertUser(message, alertType) {
    $('#alertWrapper').append('<div id="alertdiv" class="alert alert-' + alertType + ' alert-dismissible">' +
      '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
      '<span class="alertMessage">' + message + '</span></div>'
    );

    setTimeout(function () { // this will automatically close the alert and remove this if the users doesnt close it in 5 secs
        $('#alertdiv').remove();
    }, 5000);
}

function attributeTable(t, attributes) {

    var table;
    if ( $.fn.dataTable.isDataTable( t ) ) {
        table = $(t).DataTable();
    }
    else {
        table = $(t).DataTable( {
            paging : false,
            searching : false,
            info: false
        } );
    }

    table.clear();
    for (var property in attributes) {
        if (attributes.hasOwnProperty(property)) {
            table.row.add([
                '<code>' + property + '</code>', '<code>' + attributes[property] + '</code>'
            ]).draw(false);
        }
    }
}

function date(d) {
    var date = new Date(d[0],d[1]-1,d[2],d[3],d[4],d[5]);
    return date;
}

function confirm(decisionId, service) {
    $('#confirmdiv').remove();
    var svcStr = service.length > 70 ? service.substr(0,68) + '...' : service;
    var message = strings.confirm.replace('{}', svcStr);
    $('#alertWrapper').append('<div id="confirmdiv" class="alert alert-warning">' +
      '<span class="alertMessage">' + message + '</span><br/>' +
      '<button type="button" id="delete" class="btn btn-xs btn-danger" aria-label="Yes"><strong>' +
      strings.yes + ' </strong></button>' +
      '<button type="button" class="btn btn-xs btn-default" aria-label="No" value="' + decisionId + '"><strong>' +
      strings.no + '</strong></button></div>'
    );
    $('#confirmdiv .btn').click(function() {
        $('#confirmdiv').alert('close');
    });
    $('#delete').click(function() {
        removeDecision(decisionId);
    });
}

function removeDecision(decisionId) {
    var factory = {};
    factory.httpHeaders = {};
    factory.httpHeaders[$('meta[name=\'_csrf_header\']').attr('content')] = $('meta[name=\'_csrf\']').attr('content');

    $.ajax({
        type: 'post',
        url: urls.delete,
        data: {decisionId: decisionId},
        headers: factory.httpHeaders,
        dataType: 'json',
        success: function (data) {
            // Reinitialize the table data
            $('#consentDecisions').DataTable().ajax.reload();

            if (!data) {
                alertUser(strings.error, 'danger');
            } else {
                alertUser(strings.success, 'success');
                // Reload the page
                location.reload();
            }
        },
        error: function () {
            alertUser('There appears to be an error. Please try your request again.', 'danger');
        }
    });
}

var consentDecisions = (function () {
    var createDataTable = function () {
        $('#consentDecisions').DataTable({
            'order': [[0, 'desc']],
            'initComplete': function (settings, json) {
                if (!json || json.length == 0) {
                    $('#consent-decisons').hide();
                    $('#loadingMessage').hide();
                    $('#no-consent-decisions').show();
                } else {
                    $('#loadingMessage').hide();
                    $('#no-consent-decisions').hide();
                    $('#consent-decisons').show();
                }
            },
            'language': strings.data,
            'paging': false,
            'ajax': {
                'url': urls.getConsentDecisions,
                'dataSrc': ''
            },
            'data': consentDecisions,
            'columnDefs': [
                {
                    'targets': 0,
                    'className': 'created-date',
                    'data': function (row) {
                        return date(row.decision.createdDate);
                    },
                    'render': function (data) {
                        var opts = { year: 'numeric', month: 'numeric' };
                        return '<div class="label label-primary"><span class="hidden">' + data.toISOString() +
                                '</span>' + data.toLocaleDateString('en', opts ) +
                            '</div>';
                    }
                },
                {
                    'targets': 1,
                    'data': 'decision.service',
                    'className': 'col service-id',
                    'render': function (data) {
                        if ($(window).width() <= 855) {
                            return data.length > 70 ?
                                '<span title="' + data + '">' + data.substr(0, 68) + '...</span>' : data;
                        } else {
                            return data.length > 180 ?
                                '<span title="' + data + '">' + data.substr(0, 178) + '...</span>' : data;
                        }
                    }
                }
            ]
        });
    };

    var addEventHandlers = function () {
        
        /* Performs logout for consent application, no SLO */
        $('#logout').click(function() {
            var logout = window.location + '/logout';
            window.location.assign(logout);
        });
        
        // Add event listener for opening and closing details
        $(document).on('click', '#consentDecisions > tbody > tr:not(.info)', function () {
            var table = $('#consentDecisions').DataTable();
            var tr = $(this);
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
        createDataTable();
        addEventHandlers();
    })();

    // Public Methods
    return {
        /**
         * Not used
         */
    };
})();
