import org.apereo.cas.configuration.model.support.delegation.*
import org.apereo.cas.web.*
import java.util.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def providers = args[3] as Set<DelegatedClientIdentityProviderConfiguration>
    def appContext = args[4]
    def logger = args[5]

    def provider = providers.first()
    logger.info("Checking ${provider.name}...")
    provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
    provider
}
