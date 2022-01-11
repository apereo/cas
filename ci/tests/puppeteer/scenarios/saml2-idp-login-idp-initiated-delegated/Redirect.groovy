import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.apereo.cas.configuration.model.support.pac4j.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def provider = args[3] as DelegatedClientIdentityProviderConfiguration
    def logger = args[4]

    def request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext)
    def cname = request.getParameter("CName") as String
    logger.info("Checking ${provider.name} against CName=${cname}...")
    if ("CasClient".equalsIgnoreCase(cname)) {
        provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
        logger.info("Auto-redirect set for ${provider.name}...")
    }
    provider
}
