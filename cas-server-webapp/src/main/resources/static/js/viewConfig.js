
var viewConfigs = (function () {
    var createDataTable = function() {
        $('#viewConfigsTable').DataTable( {
            "initComplete": function(settings, json) {
                if (!json) {
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
                "url": urls.getConfiguration,
                "dataSrc": function (json) {
                    var return_data = new Array();
                    for (var section in json) {
                        var sectionName;
                        if (section.indexOf("applicationConfig") != -1) {
                            sectionName = "applicationConfig";
                        } else {
                            sectionName = section;
                        }
                        var object = json[section];
                        for (var item in object) {
                            return_data.push({
                                'key': sectionName + "." + item,
                                'value'  : object[item],
                            })
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
