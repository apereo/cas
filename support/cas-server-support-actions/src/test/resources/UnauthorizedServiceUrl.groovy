def run(Object[] args) {
    def registeredService = args[0]
    def requestContext = args[1]
    def applicationContext = args[2]
    def logger = args[3]

    logger.debug("Calculating url...")
    new URI("https://apereo.org/cas")
}
