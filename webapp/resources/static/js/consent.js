/* global data */
$(document).ready(function(){
    resolveAttributes();
});

function resolveAttributes() {
    var table;

    if ( $.fn.dataTable.isDataTable( '#attributesTable' ) ) {
        table = $('#attributesTable').DataTable();
    }
    else {
        table = $('#attributesTable').DataTable( {
            paging : false,
            searching : false
        } );
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
