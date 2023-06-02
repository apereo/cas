def run(Object[] args) {
    def service = args[0]
    def registeredService = args[1]
    def queryString = args[2]
    def headers = args[3]
    def logger = args[4]

    logger.debug("Checking for theme per {}", registeredService)
    return "some-theme"
}
