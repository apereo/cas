var infoEl = $('tr.info').clone();
var strings = strings;

/* Formatting function for row details - modify as you need */
function format(d) {
    var detail = infoEl.clone();
    $(detail).toggleClass('hidden');
    var dec = d.decision;
    detail.find('.created-date')[0].append(date(dec.createdDate).toLocaleString());
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
    attributeTable(detail.find('.consent-attributes'),d.attributes);
    detail.find('.consent-options [data-toggle="tooltip"]').tooltip();
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
    var date = new Date(d[0],d[1],d[2],d[3],d[4],d[5]);
    return date;
}

function confirm(decisionId, service) {
    $('#confirmdiv').remove();
    var message = strings.confirm.replace('{}', service);
    $('#alertWrapper').append('<div id="confirmdiv" class="alert alert-warning">' +
      '<span class="alertMessage">' + message + '</span>' +
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
            'order': [[1, 'desc']],
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
            'processing': true,
            'ajax': {
                'url': urls.getConsentDecisions,
                'dataSrc': ''
            },
            'data': consentDecisions,
            'columnDefs': [
                {
                    'targets': 0,
                    'className': 'details-control',
                    'orderable': false,
                    'data': null,
                    'defaultContent': ''
                },
                {
                    'targets': 1,
                    'data': 'decision.service',
                    'className': 'col',
                    'render': function (data) {
                        return data.length > 60 ?
                            '<span title="' + data + '">' + data.substr(0, 58) + '...</span>' :
                            data;
                    }
                },
                {
                    'targets': 2,
                    'data': function (row) {
                        return { 'id': row.decision.id, 'service': row.decision.service };
                    },
                    'className': 'col-1',
                    'render': function (data) {                        
                        return '<button type="button" class="btn btn-xs btn-danger" name="' + data.id + 
                          '" value="' + data.service + '"><strong>'+ strings.delete +'</strong></button>';
                    }
                },
            ]
        });
    };

    var addEventHandlers = function () {
        
        /* Performs logout for consent application, no SLO */
        $('#logout').click(function() {
            var logout = window.location + '/logout';
            window.location.assign(logout);
        });
        
        /**
         * Individual removal button
         */
        $(document).on('click', '#consentDecisions tbody tr td:last-child button.btn-danger', function (e) {
            e.preventDefault();
            confirm(this.name, this.value);
        });
        
        // Add event listener for opening and closing details
        $(document).on('click', '#consentDecisions tbody td.details-control', function () {
            var table = $('#consentDecisions').DataTable();
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
