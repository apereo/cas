def run(Object[] args) {
    def requestContext = args[0]
    def applicationContext = args[1]
    def logger = args[2]
    logger.info("Decorating the login view...")
    requestContext.flowScope.put("decoration", "decoration-results");
}
