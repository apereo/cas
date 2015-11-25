var ssoSessions = (function () {
    var urls = {
        destroy: {
            all: '/cas/statistics/ssosessions/destroySsoSessions',
            single: '/cas/statistics/ssosessions/destroySsoSession'
        },
        getSessions: '/cas/statistics/ssosessions/getSsoSessions'
    };

    var createDataTable = function() {
        $('#ssoSessions').DataTable( {
            "order": [[ 3, "desc" ]],
            "initComplete": function(settings, json) {

                if (!json || json.activeSsoSessions.length == 0) {
                    $('#loadingMessage').hide();
                    $('#no-cas-sessions').show();
                } else {
                    updateAdminPanels( json );

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
            "ajax": {
                "url": '/cas/statistics/ssosessions/getSsoSessions',
                "dataSrc": "activeSsoSessions"
            },

            columnDefs: [
                {
                    "targets": 0,
                    "className":      'details-control',
                    "orderable":      false,
                    "data":           null,
                    "defaultContent": ''
                },
                {
                    "targets": 1,
                    "data": 'is_proxied',
                    'className': 'col-xs-2 col-md-1',
                    "render" : function ( data, type, full, meta ) {
                        if ( data === true) {
                            return '<span class="label label-primary">Proxy</span>';
                        } else {
                            return ' ';
                        }
                    }
                },
                {
                    "targets": 2,
                    "data": 'authenticated_principal',
                    "className": 'col-xs-4 col-md-2',
                    "render": function ( data, type, full, meta ) {
                        return type === 'display' && data.length > 20 ?
                        '<span title="'+data+'">'+data.substr( 0, 18 )+'...</span>' :
                        data;
                    }
                },
                {
                    "targets": 3,
                    "data": 'ticket_granting_ticket',
                    "className": 'hidden-xs hidden-sm col-md-4',
                    "render": function ( data, type, full, meta ) {
                        return type === 'display' && data.length > 20 ?
                        '<span title="'+data+'">'+data.substr( 0, 40 )+'...</span>' :
                        data;
                    }
                },
                {
                    "targets": 4,
                    "data": 'authentication_date_formatted',
                    "className": 'col-xs-4 col-sm-4 col-md-2'
                },
                {
                    "targets": 5,
                    "data": 'number_of_uses',
                    "className": 'hidden-xs hidden-sm visible-md-* col-md-2'
                },
                {
                    "targets": 6,
                    "data": "ticket_granting_ticket",
                    "className": 'col-xs-2 col-sm-2 col-md-1',
                    "render": function (data, type, full, meta ) {
                        return '<button class="btn btn-xs btn-block btn-danger" type="button" value="' + data + '">Destroy</button>';
                    },
                    "orderable": false
                },
            ]
        } );
    };

    var addEventHandlers = function() {

        /**
         * The Bulk remove button
         */
        $('#removeAllSessionsButton').on('click', function(e) {
            e.preventDefault();
            removeSession(this.value);
        });

        /**
         * Individual removal button
         */
        $(document).on('click', '#ssoSessions tbody tr td:last-child button.btn-danger', function (e) {
            e.preventDefault();
            removeSession( this.value );
        });

        /**
         * The filter buttons
         */
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

            var searchTerm = table.column( 1 ).search(filterRegex, true, false).draw();

            $('#removeAllSessionsButton').val( deleteValue ).html(btnText.replace('xx', searchTerm.page.info().recordsDisplay ))
        });


        // Add event listener for opening and closing details
        $(document).on('click', '#ssoSessions tbody td.details-control', function () {
            var table = $('#ssoSessions').DataTable();
            var tr = $(this).closest('tr');
            var row = table.row( tr );

            if ( row.child.isShown() ) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            }
            else {
                // Open this row
                row.child( format(row.data()), 'info' ).show();
                tr.addClass('shown');
            }
        } );



    };

    // initialization *******
    ( function init () {
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
