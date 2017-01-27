

$(document).ready(function(){
    $('#fm1').on('submit', function(e){
        e.preventDefault();
        var uid = $('#uid').val();
        if (uid != null && uid != "") {
            resolveAttributes(uid);
        } 
    });

    if ( $.fn.dataTable.isDataTable( '#attributesTable' ) ) {
        table = $('#attributesTable').DataTable();
    }
    else {
        table = $('#attributesTable').DataTable( {
            paging: false,
            searching   : false
        } );
    }
});

function resolveAttributes(uid) {
    $.ajax({
        type: 'post',
        url: urls.resolveAttributes,
        data: {"uid": uid},
        success: function (data, status) {
            var table = $('#attributesTable').DataTable();
            table.clear();
            table.row.add([
                uid, uid
            ]).draw(false);
        }
    });
}
