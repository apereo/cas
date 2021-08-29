import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def provider = args[3] as DelegatedClientIdentityProviderConfiguration
    def logger = args[4]
    logger.info("Checking provider ${provider.name}...")
     if (service != null && service.id.startsWith("https://github.com/apereo/cas")) {
         provider.autoRedirect = true
         logger.info("Selected primary provider ${provider.name}")
         return provider
     }
    return null
}
