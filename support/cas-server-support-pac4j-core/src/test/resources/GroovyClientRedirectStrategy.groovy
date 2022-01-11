import org.apereo.cas.configuration.model.support.delegation.*
import org.apereo.cas.web.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def provider = args[3] as DelegatedClientIdentityProviderConfiguration
    def logger = args[4]
    logger.info("Checking ${provider.name}...")
    provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
    provider
}
