URI run(Object[] args) {
    def registeredService = args[0]
    def authentication = args[1]
    def requestContext = args[2]
    def applicationContext = args[3]
    def logger = args[4]
    logger.debug("Calculating url for ${authentication.principal.id}")
    new URI("https://apereo.org/cas")
}
