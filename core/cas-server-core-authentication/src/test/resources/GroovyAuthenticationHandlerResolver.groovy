def run(Object[] args) {
    def handlers = args[0]
    def transaction = args[1]
    def servicesManager = args[2]
    def logger = args[3]

    logger.trace("Resolving authentication handlers ${handlers}...")
    handlers
}

def supports(Object[] args) {
    def handlers = args[0]
    def transaction = args[1]
    def servicesManager = args[2]
    def logger = args[3]
    true
}
