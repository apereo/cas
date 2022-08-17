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

    def cname = requestContext.getRequestParameters().get("CName") as String
    providers.forEach(provider -> {
        logger.info("Checking ${provider.name} against CName=${cname}...")
        if ("CasClient".equalsIgnoreCase(cname)) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
            logger.info("Client auto-redirect set for ${provider.name}...")
            return provider
        }
        if ("CasClientFancy".equalsIgnoreCase(cname)) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.SERVER
            logger.info("Server auto-redirect set for ${provider.name}...")
            return provider
        }

        if ("CasClientNone".equalsIgnoreCase(cname)) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.NONE
            logger.info("No auto-redirect set for ${provider.name}...")
            return provider
        }
    })
    return null

}
