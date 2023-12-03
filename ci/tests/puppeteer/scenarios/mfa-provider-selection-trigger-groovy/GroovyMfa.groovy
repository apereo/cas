def run(final Object... args) {
    def service = args[0]
    def registeredService = args[1]
    def authentication = args[2]
    def httpRequest = args[3]
    def logger = (Logger) args[4]

    logger.info("Returning composite/chained MFA provider id from script...")
    return ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER
}
