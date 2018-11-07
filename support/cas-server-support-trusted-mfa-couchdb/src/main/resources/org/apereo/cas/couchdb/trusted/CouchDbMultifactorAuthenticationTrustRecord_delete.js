function(doc, req){
    if (!doc){
        return [null, "No record supplied."]
    } else {
        doc['_deleted'] = true
        return [doc, "Record Deleted."]
    }
}
