var table;

$(document).ready(function () {
    $('#fmrel').on('submit', function (e) {
        e.preventDefault();
        var username = $('#username').val();
        var password = $('#password').val();
        var service = $('#service').val();

        releaseAttributes(username, password, service);
    });


    $('#fm1').on('submit', function (e) {
        e.preventDefault();
        var uid = $('#uid').val();
        table = $('#attributesTable').DataTable();
        table.clear().draw();

        var status = $('#status');

        if (uid !== null && uid !== '') {
            resolveAttributes(uid);
            status.html('Resolved attributes for username <strong>' + uid + '</strong>.');
            status.removeClass('alert-danger');
            status.addClass('alert-info');
            status.show();
        } else {
            status.html('No username is provided.');
            status.removeClass('alert-info');
            status.addClass('alert-danger');
            status.show();
        }
    });

    if ($.fn.dataTable.isDataTable('#attributesTable')) {
        table = $('#attributesTable').DataTable();
    } else {
        table = $('#attributesTable').DataTable({
            paging: false,
            searching: false
        });
    }

    $('#status').hide();

});

function resolveAttributes(uid) {
    $.ajax({
        type: 'post',
        url: urls.resolveAttributes,
        data: {'uid': uid},
        success: function (data) {
            var table = $('#attributesTable').DataTable();
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
    });
}

function releaseAttributes(uid, psw, service) {
    $('validationresponse').empty();
    $('cas1').empty();
    $('cas2').empty();
    $('cas3Xml').empty();
    $('cas3Json').empty();

    $('#submitRelease').attr('disabled', 'disabled');
    $.ajax({
        type: 'post',
        url: urls.releaseAttributes,
        data: {'username': uid, 'password': psw, 'service': service},
        success: function (data) {
            var html = '<ul><li>Service Id: <code>' + data.registeredService.id + '</code></li>'
                + '<li>Service Identifier: <code>' + data.registeredService.serviceId + '</code></li>'
                + '<li>Service Name: <code>' + data.registeredService.name + '</code></li>'
                + '</ul><p/>';
            $('#validationresponse').html(html);

            var resp = '<pre>' + JSON.stringify(data.registeredService, null, 4) + '</pre>';
            $('#serviceJson').html(resp);

            resp = '<pre>' + data.cas1Response + '</pre>';
            $('#cas1').html(resp);

            resp = '<pre>' + data.cas2Response + '</pre>';
            $('#cas2').html(resp);

            resp = '<pre>' + data.cas3XmlResponse + '</pre>';
            $('#cas3Xml').html(resp);

            resp = '<pre>' + data.cas3JsonResponse + '</pre>';
            $('#cas3Json').html(resp);
        },
        error: function (err) {
            var html = '<div class=\'alert alert-danger\'>'
                + '<h4>Response Error</h4>'
                + 'Status: <code>' + err.responseJSON.status + '</code><p/>'
                + 'Exception: <code>' + err.responseJSON.exception + '</code><p/>'
                + 'Message: <code>' + err.responseJSON.message + '</code><p/>'
                + '</div>';
            $('#validationresponse').html(html);
        },
        complete: function() {
            $('#submitRelease').removeAttr('disabled');
        }
    });
}
