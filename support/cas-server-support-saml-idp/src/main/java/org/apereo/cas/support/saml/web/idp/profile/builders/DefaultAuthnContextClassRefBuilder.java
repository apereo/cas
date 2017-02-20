package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is {@link DefaultAuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultAuthnContextClassRefBuilder implements AuthnContextClassRefBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthnContextClassRefBuilder.class);
    @Override
    public String build(final Assertion assertion, final AuthnRequest authnRequest,
                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                        final SamlRegisteredService service) {
        final RequestedAuthnContext requestedAuthnContext = authnRequest.getRequestedAuthnContext();
        if (requestedAuthnContext == null) {
            LOGGER.debug("No specific authN context is requested. Returning [{}]", AuthnContext.UNSPECIFIED_AUTHN_CTX);
            return AuthnContext.UNSPECIFIED_AUTHN_CTX;
        }
        final List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        if (authnContextClassRefs == null || authnContextClassRefs.isEmpty()) {
            LOGGER.debug("Requested authN context class ref is unspecified. Returning [{}]", AuthnContext.UNSPECIFIED_AUTHN_CTX);
            return AuthnContext.UNSPECIFIED_AUTHN_CTX;
        }
        LOGGER.debug("AuthN Context comparison is requested to use [{}]", requestedAuthnContext.getComparison());
        authnContextClassRefs.forEach(authnContextClassRef -> LOGGER.debug("Requested AuthN Context [{}]", authnContextClassRef.getAuthnContextClassRef()));
        if (StringUtils.isNotBlank(service.getRequiredAuthenticationContextClass())) {
            LOGGER.debug("Using [{}] as indicated by SAML registered service [{}]",
                    service.getRequiredAuthenticationContextClass(),
                    service.getName());
            return service.getRequiredAuthenticationContextClass();
        }
        LOGGER.debug("Returning default AuthN Context [{}]", AuthnContext.PPT_AUTHN_CTX);
        return AuthnContext.PPT_AUTHN_CTX;
    }
}
