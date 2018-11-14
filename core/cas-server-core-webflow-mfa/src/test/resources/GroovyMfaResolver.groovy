def run(Object[] args) {
    def service = args[0]
    def registeredService = args[1]
    def authentication = args[2]
    def httpRequest = args[3]
    def logger = args[4]

    logger.info("Testing MFA")
    return "mfa-dummy"
}
