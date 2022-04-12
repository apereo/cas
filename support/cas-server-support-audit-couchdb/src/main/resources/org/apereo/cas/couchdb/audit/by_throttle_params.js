function(doc) {
    if (doc.clientIpAddress && doc.principal && doc.actionPerformed && doc.applicationCode && doc.whenActionWasPerformed){
        emit([doc.clientIpAddress, doc.principal, doc.actionPerformed, doc.applicationCode, doc.whenActionWasPerformed], doc)
    }
}

