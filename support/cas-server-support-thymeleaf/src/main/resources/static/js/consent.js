var strings = strings;

/* global data */
$(document).ready(function () {
    resolveAttributes();
    optionSelected();
    tabify(mdc);
});

function tabify(material) {
    var elm = document.getElementById('consent-tabs');
    var tabs = material.tabBar.MDCTabBar.attachTo(elm);

    tabs.listen('MDCTabBar:activated', function (ev) {
        var index = ev.detail.index;
        $('.consent-tab').addClass('d-none');
        $('#consent-tab-' + index).removeClass('d-none');
    });

    tabs.foundation_.adapter_.activateTabAtIndex(0);

    return tabs;
}




function optionSelected() {
    var v = $('input[name=option]:checked', '#fm1').val();
    if (v == 0) {
        $('#reminderPanel').hide();
        $('#reminderTab').hide();
    } else {
        $('#reminderPanel').show();
        $('#reminderTab').show();
    }
}

function resolveAttributes() {
    var table;

    if ($.fn.dataTable.isDataTable('#attributesTable')) {
        table = $('#attributesTable').DataTable();
    }
    else {
        table = $('#attributesTable').DataTable({
            paging: false,
            searching: false,
            language: {
                info: strings.info
            }
        });
    }

    table.clear();
    var attrs = data.attributes;
    for (var property in attrs) {
        if (attrs.hasOwnProperty(property)) {
            table.row.add([
                '<code>' + property + '</code>', '<code>' + attrs[property] + '</code>'
            ]).draw(false);
        }
    }
}
