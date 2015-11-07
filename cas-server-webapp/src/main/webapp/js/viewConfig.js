var viewConfigs = (function () {
    var createDataTable = function() {
        $('#viewConfigsTable').DataTable( {
            "initComplete": function(settings, json) {
                if (!json || json.length == 0) {
                    $('#loadingMessage').hide();
                    $('#viewConfigError').show();
                    $("#view-configuration").hide();
                } else {
                    $('#loadingMessage').hide();
                    $('#viewConfigError').hide();
                    $("#view-configuration").show();
                }
            },
            "drawCallback": function( settings ) {
                var api = this.api();
                if (api.page.info().pages > 1) {
                    $('#' + $.fn.dataTable.tables()[0].id + '_paginate')[0].style.display = "block";
                } else {
                    $('#' + $.fn.dataTable.tables()[0].id + '_paginate')[0].style.display = "none";
                }
            },
            "processing": true,
            "ajax": {
                "url": '/cas/status/config/getProperties',
                "dataSrc": function (json) {
                    var return_data = new Array();
                    for(var i=0;i< json.length; i++){
                        var obj = json[i];
                        for (var key in obj) {
                            if (obj.hasOwnProperty(key)) {
                                return_data.push({
                                    'key': key,
                                    'value'  : obj[key],
                                })
                            }
                        }
                    }
                    return return_data;
                }
            },
            "columns": [
                { "data": "key", 'className': 'col-xs-6' },
                { "data": "value", 'className': 'col-xs-6' }
            ],
        } );
    };
    // initialization *******
    ( function init () {
        createDataTable();
    })();

    // Public Methods
    return {
        /**
         * Not used
         */
    };
})();
