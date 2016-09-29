$('#myTabs a').click(function (e) {
    e.preventDefault()
    $(this).tab('show')
})

var authnEvents = (function () {

    var getData = function () {
        $.getJSON(urls.getRecords, function (data) {
            authnEventsTable(data);
        });
    };

    var authnEventsTable = function (jsonData) {
        var t = $('#authnEventsTable').DataTable({
            "order": [[2, "desc"]],
            retrieve: true,
            columnDefs: [

            ]
        });
        for (var i = 0; i < jsonData.length; i++) {

            /*
            var rec = jsonData[i];
            t.row.add([
                rec.name,
                rec.principal,
                new Date(rec.date),
                rec.geography,
                "<button class='btn btn-sm btn-danger' disabled type='button' value='ALL'>Revoke</button>"
            ]).draw(false);
            */
        }
    };

    // initialization *******
    (function init() {
        getData();
    })();
})();
