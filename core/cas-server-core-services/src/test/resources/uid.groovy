def run(Object[] args) {
    def attributes = args[0]
    def id = args[1]
    def service = args[2]
    def logger = args[3]
    logger.info("Testing username attribute for attributes $attributes and service ${service.id}")
    return "fromscript"
}
