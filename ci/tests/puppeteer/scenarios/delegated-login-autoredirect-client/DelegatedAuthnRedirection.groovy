import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.apereo.cas.configuration.model.support.pac4j.*
import org.apereo.cas.configuration.model.support.delegation.*
import java.util.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def providers = args[3] as Set<DelegatedClientIdentityProviderConfiguration>
    def appContext = args[4]
    def logger = args[5]

    providers.forEach(provider -> {
        logger.info("Checking provider ${provider.name} for service ${service?.id}...")
        if (service != null && service.id.startsWith("https://localhost:9859/anything/sample")) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
            logger.info("Selected primary provider ${provider.name}")
            return provider
        }
    })
    return null
}
