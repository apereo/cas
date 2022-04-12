function(doc) {
    if (doc.whenActionWasPerformed && doc.principal) {
        let d = new Date(doc.whenActionWasPerformed).getTime() + doc.principal
        emit(d, doc)
    }
}
