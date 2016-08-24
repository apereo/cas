showLogs("")
var stompClient = null;

function setConnected(connected) {

    var el = document.getElementById('websocketStatus');
    el.style.visibility = connected ? 'visible' : 'hidden';
    el.class = connected ? 'alert alert-info' : 'alert alert-danger';

    if (!connected) {
        el.innerHTML = "Disconnected!";
    } else {
        el.innerHTML = "Connected to CAS. Streaming logs from " + logConfigFileLocation + "...";
    }
}

function connect() {
    $("#logoutputarea").empty();
    var socket = new SockJS(urls.logOutput);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        stompClient.subscribe('/logs/logoutput', function (msg) {
            if (msg != null && msg.body != "") {
                showLogs(msg.body);
            }
        });
    });
}

function disconnect() {
    $("#logoutputarea").empty();
    $("#logoutputarea").attr('readonly', 'readonly');

    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
}

function getLogs() {
    stompClient.send(urls.logOutput, {}, {});
}

function showLogs(message) {
    var response = document.getElementById('logoutputarea');
    if (message != "") {
        response.value += message + "\n";
    }
    response.scrollTop = response.scrollHeight
}

disconnect();
connect();
setInterval(function () {
    getLogs();
}, 1000);

/*************
 *
 ***************/
$('#myTabs a').click(function (e) {
    e.preventDefault()
    $(this).tab('show')
})

var alertHandler = (function () {
    var alertContainer = $('#alert-container');
    var create = function (message, state) {
        //console.log('create the alert');
        alertContainer.html('<div class="alert alert-' + state + ' alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + message + '</span></div>')
    };

    var destroy = function () {
        alertContainer.empty();
    };

    return {
        dismiss: function () {
            console.log('dismiss the alert');
            destroy();
        },
        show: function (msg, state) {
            console.log('show the alert', 'msg:', msg, 'state:', state);
            create(msg, state);
        }
    }
})();

var loggingDashboard = (function () {
    var json = null;

    var logLevels = ['trace', 'debug', 'info', 'warn', 'error'];

    var getData = function () {
        $.getJSON(urls.getConfiguration, function (data) {
            json = data;
            loggerTable();
        });
    };


    var loggerTable = function () {
        $('#loggersTable').DataTable({
            "order": [[1, "desc"]],
            data: json.loggers,
            "drawCallback": function (settings) {
                var api = this.api();

                if (api.page.info().pages > 1) {
                    $('#' + $.fn.dataTable.tables()[0].id + '_paginate')[0].style.display = "block";
                } else {
                    $('#' + $.fn.dataTable.tables()[0].id + '_paginate')[0].style.display = "none";
                }
            },
            "initComplete": function (settings, data) {
                if (!settings.aoData || settings.aoData.length == 0) {
                    $('#loadingMessage').addClass('hidden');
                    $('#errorLoadingData').removeClass('hidden');
                } else {
                    $('#loadingMessage').addClass('hidden');
                    $('#errorLoadingData').addClass('hidden');
                    $("#loggingDashboard .tabsContainer").removeClass('hidden');
                }
            },
            "processing": true,
            columnDefs: [
                {
                    "targets": 0,
                    "className": 'details-control',
                    "orderable": false,
                    "data": 'appenders',
                    "defaultContent": '',
                    render: function (data, type, full, meta) {
                        if (data.length > 0) {
                            return '<span></span>';
                        } else {
                            return '';
                        }
                    }
                },
                {
                    targets: 1,
                    data: 'name',
                    className: 'col-xs-5'
                },
                {
                    targets: 2,
                    data: 'additive',
                    className: 'additive col-xs-2',
                    render: function (data, type, full, meta) {
                        if (data) {
                            return '<span class="glyphicon glyphicon-ok" aria-hidden="true"></span>';
                        } else {
                            return '<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>';

                        }
                    }
                },
                {
                    targets: 3,
                    data: 'state',
                    className: 'col-xs-2'
                },
                {
                    targets: 4,
                    data: 'level',
                    className: 'col-xs-3',
                    render: function (data, type, full, meta) {
                        return toggleSwitch(data, type, full, meta);
                    }
                }
            ]
        });
    };

    var toggleSwitch = function (data, type, full, meta) {
        // Todo: Add additional colors for the other options
        //console.log('toggleSwitch data',data);
        //console.log('type',type);
        //console.log('full',full);
        //console.log('meta',meta);
        //console.log(logLevels);
        var btnColor;

        switch (data.toLowerCase()) {
            case 'error':
                btnColor = 'danger';
                break;
            case 'info':
                btnColor = 'info';
                break;
            case 'warn':
                btnColor = 'warning';
                break;
            default:
                btnColor = 'default';
        }
        var btnGroup = '<div class="btn-group btn-block" data-logger="' + full + '"><button class="btn btn-sm btn-block bg-' + btnColor + ' dropdown-toggle" name="recordinput" data-toggle="dropdown">' + data + ' <span class="caret"></span></button>' +
            '<ul class="dropdown-menu">';
        for (var i = 0; i < logLevels.length; i++) {
            btnGroup += '<li><a href="#">' + logLevels[i].toUpperCase() + '</a></li>';
        }
        btnGroup += '</ul></div>';

        return btnGroup;
    };

    /* Formatting function for row details - modify as you need */
    var viewAppenders = function (data) {
        alert(data.appenders)
        return '<table class="table table-bordered row-detail"><tbody><tr class="">' +
            '<td class="field-label active">Appenders:</td>' +
            '<td><kbd>' + JSON.stringify(data.appenders, null, 2) + '</kbd></td>' +
            '</tr>' +
            '</tbody></table>';
    };

    var addEventHandlers = function () {
        //console.log('addEventHAndlers()');

        $(document).on('click', '#loggersTable .dropdown-menu li a', function (e) {
            //console.log('status change', this);
            e.preventDefault();
            var selText = $(this).text();

            changeLogLevel(selText, this);
        });

        $(document).on('click', '#loggersTable tbody td.details-control span', function () {
            var table = $('#loggersTable').DataTable();
            var tr = $(this).closest('tr');
            var row = table.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            } else {
                // Open this row
                row.child(viewAppenders(row.data()), 'info').show();
                tr.addClass('shown');
            }
        });
    };

    var changeLogLevel = function (newLevel, el) {
        /**
         * POST - /cas/status/logging/updateLoggerLevel
         * Allows you to change the log level for given logger. Parameters are:
         * loggerName, loggerLevel, additive (true/false)
         */
        var table = $('#loggersTable').DataTable();
        var data = table.row($(el).closest('tr')[0]).data();

        var cell = table.cell($(el).closest('td')[0]);

        var jqxhr = $.post(urls.updateLevel, {
            loggerName: data.name,
            loggerLevel: newLevel,
            additive: data.additive
        }, function () {
            cell.data(newLevel).draw();
            alertHandler.show('Successfully changed.', 'success');
        }).fail(function () {
            alertHandler.show('Error saving change.  Please try again', 'danger');
        });
    };

    // initialization *******
    (function init() {
        getData();
        addEventHandlers();
    })();

    return {
        getJson: function () {
            return json;
        },
        showLoggersTable: function () {
            loggerTable();
        }
    }
})();
