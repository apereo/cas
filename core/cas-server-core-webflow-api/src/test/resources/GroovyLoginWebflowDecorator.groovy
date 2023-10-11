def run(Object[] args) {
    def requestContext = args[0]
    def logger = args[1]
    logger.info("Decorating the login view...")
    requestContext.flowScope.put("decoration", "decoration-results")
}
