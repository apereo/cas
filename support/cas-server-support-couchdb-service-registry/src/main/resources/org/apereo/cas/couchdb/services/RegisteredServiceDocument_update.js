function(doc, req){
    if (!doc){
        if ('doc' in req && req['doc']){
            doc = req['doc']
            doc['_id'] = req.uuid
            return [doc, "New record added."]
        }
    } else if ('doc' in req && req['doc']){
        other = req['doc']
        doc['service'] = other.service
        return [doc, "Record Updated."]
    }
    return [null, "Empty record provided."]
}
