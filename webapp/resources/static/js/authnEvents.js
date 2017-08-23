$('#myTabs a').click(function (e) {
    e.preventDefault();
    $(this).tab('show');
});

(function () {
    var getData = function () {
        $.getJSON(urls.getEvents, function (data) {
            authnEventsTable(data);
        });
    };

    var authnEventsTable = function (jsonData) {
        var t = $('#authnEventsTable').DataTable({
            'order': [[2, 'desc']],
            retrieve: true,
            columnDefs: [
                {
                    'targets': 0,
                    render: function (data) {
                        return '<span class="glyphicon glyphicon-flash" aria-hidden="true">&nbsp;</span>' + data;
                    }
                }
            ]
        });
        for (var i = 0; i < jsonData.length; i++) {
            var rec = jsonData[i];

            var type = rec.type.split('.');
            t.row.add([
                type[type.length - 1],
                rec.principalId,
                new Date(rec.creationTime*1000),
                new Date(rec.timestamp),
                rec.properties.agent,
                rec.clientIpAddress,
                rec.serverIpAddress,
                rec.properties.geoLatitude === 'undefined' ? '' : Number(rec.properties.geoLatitude).toFixed(2),
                rec.properties.geoLongitude === 'undefined' ? '' : Number(rec.properties.geoLongitude).toFixed(2),
                rec.properties.geoAccuracy === 'undefined' ? '' : Number(rec.properties.geoAccuracy).toFixed(2)
            ]).draw(false);
        }
    };

    // initialization *******
    (function init () {
        getData();
    })();
})();
