
def run(final Object... args) {
    def service = args[0]
    def registeredService = args[1]
    def authentication = args[2]
    def httpRequest = args[3]
    def logger = args[4]

    logger.info("Evaluating principal attributes [{}]", authentication.principal.attributes)
    def email = authentication.principal.attributes['email'][0]
    logger.info("Found email attribute with value [{}]", email)
    if (email.matches(".+@apereo.org")) {
        logger.info("Checking service id [{}]...", service.id)
        if (service.id.endsWith("/open")) {
            logger.info("Will trigger alternative Duo Security provider")
            return "mfa-duo-alt"
        }
        logger.info("Will trigger default Duo Security provider")
        return "mfa-duo"
    }
    logger.warn("Will not trigger MFA; principal attributes do not match!")
    return null
}
