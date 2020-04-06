package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

import java.util.List;

/**
 * This is {@link DefaultAuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultAuthnContextClassRefBuilder implements AuthnContextClassRefBuilder {

    private final CasConfigurationProperties casProperties;

    @Override
    public String build(final Object assertion,
                        final RequestAbstractType authnRequest,
                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                        final SamlRegisteredService service) {
        if (StringUtils.isNotBlank(service.getRequiredAuthenticationContextClass())) {
            LOGGER.debug("Using [{}] as indicated by SAML registered service [{}]",
                service.getRequiredAuthenticationContextClass(),
                service.getName());
            return service.getRequiredAuthenticationContextClass();
        }

        val defClass = StringUtils.defaultIfBlank(
            casProperties.getAuthn().getSamlIdp().getResponse().getDefaultAuthenticationContextClass(),
            AuthnContext.PPT_AUTHN_CTX);

        val requestedAuthnContext = authnRequest instanceof AuthnRequest
            ? AuthnRequest.class.cast(authnRequest).getRequestedAuthnContext() : null;
        if (requestedAuthnContext == null) {
            LOGGER.debug("No specific authN context is requested. Returning [{}]", defClass);
            return defClass;
        }
        val authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        if (authnContextClassRefs == null || authnContextClassRefs.isEmpty()) {
            LOGGER.debug("Requested authN context class ref is unspecified. Returning [{}]", defClass);
            return defClass;
        }

        val finalCtx = StringUtils.defaultIfBlank(getAuthenticationContextByAssertion(assertion,
            requestedAuthnContext, authnContextClassRefs), defClass);
        LOGGER.debug("Returning authN context [{}]", finalCtx);
        return finalCtx;
    }

    /**
     * Gets authentication context by assertion.
     * This is more of a template method for the time being,
     * and may be enhanced later to support more advanced parsing of classes
     * from the assertion.
     *
     * @param assertion             the assertion
     * @param requestedAuthnContext the requested authn context
     * @param authnContextClassRefs the authn context class refs
     * @return the authentication context by assertion
     */
    protected String getAuthenticationContextByAssertion(final Object assertion,
                                                         final RequestedAuthnContext requestedAuthnContext,
                                                         final List<AuthnContextClassRef> authnContextClassRefs) {
        LOGGER.debug("AuthN Context comparison is requested to use [{}]", requestedAuthnContext.getComparison());
        authnContextClassRefs.forEach(c -> LOGGER.debug("Requested AuthN Context [{}]", c.getURI()));
        return null;
    }
}
