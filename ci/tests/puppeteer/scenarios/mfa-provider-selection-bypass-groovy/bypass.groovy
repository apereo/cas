def run(final Object... args) {
    def authentication = args[0]
    def principal = args[1]
    def registeredService = args[2]
    def provider = args[3]
    def logger = args[4]
    def httpRequest = args[5]
    logger.info("Bypassing multifactor for provider ${provider.id}")
    return false
}
