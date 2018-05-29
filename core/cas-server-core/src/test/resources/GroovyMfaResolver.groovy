def run(Object[] args) {
    def service = args[0]
    def registeredService = args[1]
    def authentication = args[2]
    def logger = args[3]
    logger.info("Testing MFA")
    return "mfa-dummy"
}
