import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider

def run(final Object... args) {
    def authentication = args[0]
    def registeredService = args[1]
    def httpRequest = args[2]
    def service = args[3]
    def applicationContext = args[4]
    def logger = args[5]

    logger.debug("Determine mfa provider for ${registeredService} and ${authentication}")
    TestMultifactorAuthenticationProvider.ID
}
