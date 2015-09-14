/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Created by Jeff Sittler 9/8/15.
 */

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

            var searchTerm = table.column( 0 ).search(filterRegex, true, false).draw();

            $('#removeAllSessionsButton').val( deleteValue ).html(btnText.replace('xx', searchTerm.page.info().recordsDisplay ))
        });

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