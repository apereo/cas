import java.util.*

class SampleGroovyProviderSelection {
    def run(final Object... args) {
        def service = args[0]
        def principal = args[1]
        def providersCollection = args[2]
        def logger = args[3]

        logger.debug("Resolving provider from ${providersCollection} for ${principal} with service ${service}")
        return "mfa-dummy"
    }
}
