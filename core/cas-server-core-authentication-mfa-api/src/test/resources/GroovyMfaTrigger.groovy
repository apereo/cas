import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider

class SampleGroovyEventResolver {
    def run(final Object... args) {
        def service = args[0]
        def registeredService = args[1]
        def authentication = args[2]
        def httpRequest = args[3]
        def logger = args[4]

        return TestMultifactorAuthenticationProvider.ID
    }
}
