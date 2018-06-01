def run(Object[] args) {
    def transaction = args[0]
    def logger = args[1]

    def credential = transaction.getPrimaryCredential().get()
    logger.debug("This transaction has a primary credential of ${credential.id}")
    true
}

def supports(Object[] args) {
    def credential = args[0]
    def logger = args[1]
    true
}
