def run(Object[] args) {
    def attributes = args[0]
    def id = args[1]
    def logger = args[2]
    logger.info("Testing username attribute")
    return "test"
}
