import org.apereo.cas.web.*
import org.apereo.cas.support.saml.*
import org.apereo.cas.support.saml.idp.*
import org.apereo.cas.configuration.model.support.delegation.*
import org.apereo.cas.pac4j.*
import org.apereo.cas.web.support.*
import org.apache.commons.lang3.tuple.*
import org.pac4j.core.context.*
import org.pac4j.jee.context.*
import org.opensaml.core.xml.schema.*
import org.opensaml.saml.saml2.core.*
import java.util.stream.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def providers = args[3] as Set<DelegatedClientIdentityProviderConfiguration>
    def appContext = args[4]
    def logger = args[5]

    /**
     Make sure our configuration holds SAML2
     identity providers for delegation. This is an
     extra safety check and may be removed.
     */
    if (providers.stream().noneMatch(provider -> {
        return provider.type.equalsIgnoreCase("saml2") })) {
        logger.info("No SAML2 providers found")
        return null
    }

    /**
     Minor boilerplate to get access to components that assist with locating the
     saml2 authn request sent by the SP
     */
    def request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext)
    def response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext)
    def webContext = new JEEContext(request, response)
    def sessionStore = appContext.getBean("samlIdPDistributedSessionStore")
    def openSamlConfigBean = appContext.getBean(OpenSamlConfigBean.DEFAULT_BEAN_NAME)

    /**
     Locate the SAML2 authentication request sent by the SP
     so we may examine the requested authn context class, if any.
     */
    logger.info("Locate the SAML2 authentication request sent by the SP...")
    def result = SamlIdPSessionManager.of(openSamlConfigBean, sessionStore)
            .fetch(webContext, AuthnRequest.class)
            .map(Pair::getLeft)
            .map(AuthnRequest.class::cast)

    /**
        Locate the two identity providers
     */
    def samlIdP = providers.find { it.name.equals "SAML2Client" }

    if (result.isPresent()) {
        def authnRequest = result.get()
        def requestedAuthnContext = authnRequest.getRequestedAuthnContext()
        def refs = []

        /**
         Build up a list of all requested authn context classes
         from the saml2 authentication request.
         */
        if (requestedAuthnContext != null
                && requestedAuthnContext.getAuthnContextClassRefs() != null
                && !requestedAuthnContext.getAuthnContextClassRefs().isEmpty()) {
            refs = requestedAuthnContext.getAuthnContextClassRefs()
                    .stream()
                    .map(XSURI::getURI)
                    .collect(Collectors.toList())
        }

        logger.info("Authn context classes requested are [{}]", refs)
        if (refs.contains("https://refeds.org/profile/mfa")) {
            logger.info("Found refeds MFA for provider ${samlIdP.name}")
            return samlIdP
        }
    }
    logger.info("No identity provider could be selected. Moving on...")
    return null
}
