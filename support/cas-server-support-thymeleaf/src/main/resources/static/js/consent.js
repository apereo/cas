$(document).ready(function () {
    optionSelected();
    tabify(mdc);
});


function tabify(material) {
    let elm = document.getElementById('consent-tabs');
    let tabs = material.tabBar.MDCTabBar.attachTo(elm);

    tabs.listen('MDCTabBar:activated', function (ev) {
        let index = ev.detail.index;
        $('.consent-tab').addClass('d-none');
        $('#consent-tab-' + index).removeClass('d-none');
    });

    tabs.foundation.adapter.activateTabAtIndex(0);

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
