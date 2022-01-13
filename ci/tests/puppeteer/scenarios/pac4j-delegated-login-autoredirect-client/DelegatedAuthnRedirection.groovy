import org.apereo.cas.web.*
import org.apereo.cas.configuration.model.support.pac4j.*
import org.apereo.cas.configuration.model.support.delegation.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def provider = args[3] as DelegatedClientIdentityProviderConfiguration
    def logger = args[4]
    logger.info("Checking provider ${provider.name} for service ${service?.id}...")
     if (service != null && service.id.startsWith("https://github.com/apereo/cas")) {
         provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
         logger.info("Selected primary provider ${provider.name}")
         return provider
     }
    return null
}
