import java.util.*

def Object run(final Object... args) {
    def principal = args[0]
    def principalAttributes = args[1]
    def logger = args[2]

    logger.info("Checking for impersonation authz for $principal...")

    // Decide if impersonation is allowed by returning true...
    if (principal.contains("enabled")) {
        return true
    }
    logger.warn("User is not allowed to proceed with impersonation!")
    return false
}
