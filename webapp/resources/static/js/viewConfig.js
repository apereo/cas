/* globals $, urls, updateEnabled */
(function () {
    var origData;

    var setupButtonClickOnRefreshedProperties = function () {
        $('#formRefreshCAS button').on('click', function (e) {
            e.preventDefault();

            // Show the refreshModal
            var myModal = $('#refreshModal').modal({
                keyboard: false,
                backdrop: 'static'
            });

            var primaryButton = myModal.find('.modal-footer button.btn-primary');

            // Disable the primary button
            primaryButton.prop('disabled', true).text('Refreshing...');

            $.post(e.target.parentNode.action, function (data) {
                if (data.length !== 0) {
                    $('#refreshModal-label').text('Refreshed Properties');
                    myModal.find('.modal-content .modal-body').html(
                        '<pre>' + data + '</pre>' +
            '<p>Click &quot;OK&quot; to reload page.</p>'
                    );
                } else {
                    myModal.find('.modal-header .modal-title').text('Properties Refreshed');
                    myModal.find('.modal-content .modal-body').html(
                        '<p>Click &quot;OK&quot; to reload page.</p>'
                    );
                }
            })
                .done(function () {
                    primaryButton.prop('disabled', false).text('Reload page').on('click', function (e) {
                        e.preventDefault();
                        window.location.reload();
                    });
                })
                .fail(function (jqXHR) {
                    $('#refreshModal-label').text('Problem With Refreshing Properties');
                    myModal.find('.modal-content .modal-body').html(
                        '<div class="alert alert-warning"><strong>Status: ' + jqXHR.status + '</strong><p/>Unable to refresh the properties. Please try again.</div>'
                    );
                    primaryButton.prop('disabled', false).text('OK').on('click', function (e) {
                        e.preventDefault();
                        myModal.modal('hide');
                    });
                });
        });
    };

    var createDataTable = function () {
        $('#viewConfigsTable').DataTable({
            'autoWidth': false,
            'initComplete': function (settings, json) {
                if (!json) {
                    $('#loadingMessage').hide();
                    $('#viewConfigError').show();
                    $('#view-configuration').hide();
                } else {
                    $('#loadingMessage').hide();
                    $('#viewConfigError').hide();
                    $('#view-configuration').show();
                }
            },
            'drawCallback': function () {
                var api = this.api();
                if (api.page.info().pages > 1) {
                    $('#' + $.fn.dataTable.tables()[0].id + '_paginate')[0].style.display = 'block';
                } else {
                    $('#' + $.fn.dataTable.tables()[0].id + '_paginate')[0].style.display = 'none';
                }

                if (updateEnabled) {
                    editTable();
                }
            },
            'processing': true,
            'ajax': {
                'url': urls.getConfiguration,
                'dataSrc': function (json) {
                    var returnData = [];
                    for (var item in json) {
                        returnData.push({
                            'key': '<code>' + item + '</code>',
                            'value': '' + json[item] + ''
                        });
                    }
                    return returnData;
                }
            },
            'columns': [
                {'data': 'key', 'className': 'col-xs-6 key'},
                {'data': 'value', 'className': 'col-xs-6 value'}
            ],
            'pageLength': 50
        });
    };

    var getRowData = function (row) {
        var tds = row.find('td');
        var tmp = {};
        $.each(tds, function (i) {
            if (i % 2 === 0) {
                tmp.key = $(this).text();
            } else {
                tmp.value = $(this).text();
            }
        });
        return tmp;
    };

    var editTable = function () {
        $('#viewConfigsTable').editableTableWidget({editor: $('<textarea>')});

        $('#viewConfigsTable td').on('focus', function () {
            origData = getRowData($(this).closest('tr'));
        });

        $('#viewConfigsTable tr').on('change', function () {
            var newChanges = getRowData($(this));

            var data = {old: origData, new: newChanges};
            $.ajax({url: urls.updateConfiguration, data: JSON.stringify(data), type: 'POST', contentType: 'application/json'})
                .fail(function () {
                    var result = 'Failed to save settings.';
                    $('#alertWrapper').addClass('alert-warning');
                    $('#alertWrapper').removeClass('alert-success');

                    $('#alertWrapper').text(result);
                    $('#alertWrapper').show();
                })
                .success(function () {
                    var result = 'Saved settings successfully.';
                    $('#alertWrapper').removeClass('alert-warning');
                    $('#alertWrapper').addClass('alert-success');

                    $('#resultText').text(result);
                    $('#alertWrapper').show();
                });
        });
    };

    // initialization *******
    (function init () {
        createDataTable();
        setupButtonClickOnRefreshedProperties();
    })();

    // Public Methods
    return {
    /**
     * Not used
     */
    };
})();
