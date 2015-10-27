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
 * Created by unicon on 10/19/15.
 */


/**
 * ToDo:
 * - wire up extended information view "+" functionality
 * - wire up the dropdown to show the static list of options
 * - show the selected item in from the dropdown
 * - wire up the AJAX call when a value is changed
 *  - when the ajax call is made, change the drop-down to disabled and/or a processing state
 *  - when ajax call is successful, change state to indicate it was successful or there was an error
 * - Add active loggers tab
 * -- ???
 */

$('#myTabs a').click(function (e) {
  e.preventDefault()
  $(this).tab('show')
})

var loggingDashboard = (function () {
    var json = null;

    var getData = function() {
        ///cas/status/config/logging/getConfiguration

        $.getJSON( "/cas/status/config/logging/getConfiguration", function( data ) {
            //console.log(data);
            json = data;

            loggerTable();
          //var items = [];
          //$.each( data, function( key, val ) {
          //  items.push( "<li id='" + key + "'>" + val + "</li>" );
          //});

        });
    };

    var loggerTable = function() {
        $('#loggersTable').DataTable( {
            "order": [[ 1, "desc" ]],
            data: json.loggers,
            //dataSrc: "loggers",
            "initComplete": function(settings, data) {
                //console.warn(settings.aoData);
            //
                if (!settings.aoData || settings.aoData.length == 0) {
                    $('#loadingMessage').addClass('hidden');
                    $('#errorLoadingData').removeClass('hidden');
                } else {
            //        updateAdminPanels( json );
            //
                    $('#loadingMessage').addClass('hidden');
                    $('#errorLoadingData').addClass('hidden');
                    $("#loggingDashboard .tabsContainer").removeClass('hidden');
                }
            },
            //"language": {
            //    //"infoEmpty": "No active sessions were found",
            //    "emptyTable": "No sessions found",
            //    "zeroRecords": "No matching sessions found"
            //},
            "processing": true,
            //"ajax": {
            //    "url": '/cas/statistics/ssosessions/getSsoSessions',
            //    "dataSrc": "activeSsoSessions"
            //},
            //columns: [
            columnDefs: [
                {
                    "targets": 0,
                    "orderable":      false,
                    "data":           'appenders',
                    "defaultContent": '',
                    render: function (data, type, full, meta) {
                        if (data.length > 0) {
                            return '+';
                        } else {
                            return '';
                        }
                    }
                },
                {
                    targets: 1,
                    data: 'name'
                },
                {
                    targets: 2,
                    data: 'level',
                    render: function (data, type, full, meta) {
                        return toggleSwitch(data, type, full, meta);
                        //return data
                    }
                    //render: toggleSwitch(data, type, full, meta)
                }
                //{
                //    "targets": 0,
                //    "className":      'details-control',
                //    "orderable":      false,
                //    "data":           null,
                //    "defaultContent": ''
                //},
                //{
                //    "targets": 1,
                //    "data": 'is_proxied',
                //    'className': 'col-xs-2 col-md-1',
                //    "render" : function ( data, type, full, meta ) {
                //        if ( data === true) {
                //            return '<span class="label label-primary">Proxy</span>';
                //        } else {
                //            return ' ';
                //        }
                //    }
                //},
                //{
                //    "targets": 2,
                //    "data": 'authenticated_principal',
                //    "className": 'col-xs-4 col-md-2',
                //    "render": function ( data, type, full, meta ) {
                //        return type === 'display' && data.length > 20 ?
                //        '<span title="'+data+'">'+data.substr( 0, 18 )+'...</span>' :
                //        data;
                //    }
                //},
                //{
                //    "targets": 3,
                //    "data": 'ticket_granting_ticket',
                //    "className": 'hidden-xs hidden-sm col-md-4',
                //    "render": function ( data, type, full, meta ) {
                //        return type === 'display' && data.length > 20 ?
                //        '<span title="'+data+'">'+data.substr( 0, 40 )+'...</span>' :
                //        data;
                //    }
                //},
                //{
                //    "targets": 4,
                //    "data": 'authentication_date_formatted',
                //    "className": 'col-xs-4 col-sm-4 col-md-2'
                //},
                //{
                //    "targets": 5,
                //    "data": 'number_of_uses',
                //    "className": 'hidden-xs hidden-sm visible-md-* col-md-2'
                //},
                //{
                //    "targets": 6,
                //    "data": "ticket_granting_ticket",
                //    "className": 'col-xs-2 col-sm-2 col-md-1',
                //    "render": function (data, type, full, meta ) {
                //        return '<button class="btn btn-xs btn-block btn-danger" type="button" value="' + data + '">Destroy</button>';
                //    },
                //    "orderable": false
                //},
            ]
        });
    };

    var toggleSwitch = function(data, type, full, meta) {
        //console.log('type',type);
        //console.log('full',full);
        //console.log('meta',meta);
        //console.log(logLevels);
        var btnGroup = '<div class="btn-group btn-block"><button class="btn btn-sm btn-block dropdown-toggle text-right" name="recordinput" data-toggle="dropdown">' + data + ' <span class="caret"></span></button>' +
            '<ul class="dropdown-menu">' +
                  '<li><a href="#">A</a></li>' +
                  '<li><a href="#">CNAME</a></li>' +
                  '<li><a href="#">MX</a></li>' +
                  '<li><a href="#">PTR</a></li>' +
            '</ul>' +
        '</div>';

/*
var btnGroup = '<div class="btn-group btn-block">' +
  '<button type="button" class="btn btn-default">' + data + '</button>' +
  '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
    '<span class="caret"></span>' +
    '<span class="sr-only">Toggle Dropdown</span>' +
  '</button>' +
  '<ul class="dropdown-menu">' +
    '<li><a href="#">Action</a></li>' +
    '<li><a href="#">Another action</a></li>' +
    '<li><a href="#">Something else here</a></li>' +
    '<li role="separator" class="divider"></li>' +
    '<li><a href="#">Separated link</a></li>' +
  '</ul>' +
'</div>';
*/
        //return '<button class="btn btn-xs btn-block btn-danger" type="button" value="' + data.state + '">data.state</button>'
        return btnGroup;
    };

    // initialization *******
    ( function init () {
        getData();
        //createDataTable();
    })();

    return {
        getJson: function() {
            return json;
        },
        showLoggersTable: function() {
            loggerTable();
        }
    }
})();
