import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.springframework.webflow.execution.*

def run(Object[] args) {
    def requestContext = args[0] as RequestContext
    def provider = (args[1] as Set<DelegatedClientIdentityProviderConfiguration>)[0]
    def logger = args[2]
    logger.info("Checking ${provider.name}...")
    provider.setTitle("TestTitle")
    provider.setAutoRedirect(true)

    def response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext)
    response.setStatus(302)
}
