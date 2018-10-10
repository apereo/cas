function(doc, req){
    if (!doc){
        return [null, "No record suppied."]
    } else {
        doc['_deleted'] = true
        return [doc, "Record Deleted."]
    }
}
