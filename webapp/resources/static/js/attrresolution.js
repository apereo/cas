var table;

$(document).ready(function(){
    $('#fm1').on('submit', function(e){
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

    if ( $.fn.dataTable.isDataTable( '#attributesTable' ) ) {
        table = $('#attributesTable').DataTable();
    } else {
        table = $('#attributesTable').DataTable( {
            paging: false,
            searching   : false
        } );
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
