$(document).ready(function(){

    if ( $.fn.dataTable.isDataTable( '#attributesTable' ) ) {
        table = $('#attributesTable').DataTable();
    }
    else {
        table = $('#attributesTable').DataTable( {
            paging : false,
            searching : false
        } );
    }

    resolveAttributes();
});

function resolveAttributes() {
    var table = $('#attributesTable').DataTable();
    table.clear();
    var attrs = data.attributes;
    for (var property in attrs) {
        if (attrs.hasOwnProperty(property)) {
            table.row.add([
                "<code>" + property + "</code>", "<code>" + attrs[property] + "</code>"
            ]).draw(false);
        }
    }
}
