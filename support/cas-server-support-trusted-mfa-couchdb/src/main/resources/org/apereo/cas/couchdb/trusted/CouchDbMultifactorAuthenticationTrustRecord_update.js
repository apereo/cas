function(doc, req){
    if (!doc){
        if ('doc' in req && req['doc']){
            doc = req['doc']
            doc['_id'] = req.uuid
            return [doc, "New record added."]
        }
    } else if ('doc' in req && req['doc']){
        other = req['doc']
        doc['principal'] = other.principal
        doc['deviceFingerprint'] = other.deviceFingerprint
        doc['recordDate'] = other.recordDate
        doc['recordKey'] = other.recordKey
        if ('name' in other && other['name']) {
            doc['name'] = other.name
        }
        return [doc, "Record Updated."]
    }
    return [null, "Empty record provided."]
}
