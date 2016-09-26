/*************
 *
 ***************/
$('#myTabs a').click(function (e) {
    e.preventDefault()
    $(this).tab('show')
})

var trustedDevices = (function () {

    var getData = function () {
        $.getJSON(urls.getRecords, function (data) {
            trustedDevicesTable(data);
        });
    };

    var trustedDevicesTable = function (jsonData) {
        var t = $('#trustedDevicesTable').DataTable({
            "order": [[2, "desc"]],
            retrieve: true,
            columnDefs: [

                {"width": "20%", "targets": 0},
                {"width": "10%", "targets": 1},
                {"width": "60%", "targets": 2},
                {"width": "10%", "targets": 3},
                {"width": "30%", "targets": 4}
            ]
        });
        for (var i = 0; i < jsonData.length; i++) {

            var rec = jsonData[i];
            t.row.add([
                rec.name,
                rec.principal,
                new Date(rec.date),
                rec.geography,
                "<button class='btn btn-sm btn-danger' disabled type='button' value='ALL'>Revoke</button>"
            ]).draw(false);
        }
    };

    // initialization *******
    (function init() {
        getData();
    })();
})();
