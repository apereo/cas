import org.apereo.cas.web.cookie.CookieGenerationContext

def run(final Object... args) {
    def request = args[0]
    def response = args[1]
    def context = args[2] as CookieGenerationContext
    def logger = args[3]
    logger.info("Generating same-site cookie value for ${context.name}")
    return "SameSite=Something;"
}
