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
        });
        for (var i = 0; i < jsonData.length; i++) {
            var rec = jsonData[i];
            t.row.add([
                
            ]).draw(false);
        }
    };
    
    // initialization *******
    (function init() {
        getData();
    })();
})();
