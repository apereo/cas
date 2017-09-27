var consentDecisions = (function () {
    var createDataTable = function () {
        $('#consentDecisions').DataTable({
            'order': [[1, 'desc']],
            'initComplete': function (settings, json) {
                if (!json || json.length == 0) {
                    $('#loadingMessage').hide();
                    $('#no-consent-decisions').show();
                } else {
                    $('#loadingMessage').hide();
                    $('#no-consent-decisions').hide();
                    $('#consent-decisions').show();
                }
            },
            'language': {
                'zeroRecords': 'No matching decisions found'
            },
            'processing': true,
            'ajax': {
                'url': urls.getConsentDecisions,
                'dataSrc': ''
            },
            data: consentDecisions,
            columnDefs: [
                {
                    'targets': 0,
                    'className': 'details-control',
                    'orderable': false,
                    'data': null,
                    'defaultContent': '+'
                },
                {
                    'targets': 1,
                    'data': 'service',
                    'className': 'col-xs-8 col-md-8',
                    'render': function (data) {
                        return data.length > 60 ?
                            '<span title="' + data + '">' + data.substr(0, 58) + '...</span>' :
                            data;
                    }
                },
                {
                    'targets': 2,
                    'data': 'options',
                    'className': 'col-xs-0 col-md-3',
                    'render': function (data) {
                        return '<span class="label label-primary">'+data+'</span>';
                    }
                },
            ]
        });
    };

    // initialization *******
    (function init() {
        createDataTable();
    })();

    // Public Methods
    return {
        /**
         * Not used
         */
    };
})();
