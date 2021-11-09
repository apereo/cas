package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
    public String build(final AuthenticatedAssertionContext assertion,
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

        val contextInAssertion = getAuthenticationContextByAssertion(assertion,
            requestedAuthnContext, authnContextClassRefs);
        val finalCtx = StringUtils.defaultIfBlank(contextInAssertion, defClass);
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
    protected String getAuthenticationContextByAssertion(final AuthenticatedAssertionContext assertion,
                                                         final RequestedAuthnContext requestedAuthnContext,
                                                         final List<AuthnContextClassRef> authnContextClassRefs) {
        LOGGER.debug("AuthN Context comparison is requested to use [{}]", requestedAuthnContext.getComparison());
        authnContextClassRefs.forEach(c -> LOGGER.debug("Requested AuthN Context [{}]", c.getURI()));
        val authnContexts = casProperties.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings();
        val definedContexts = CollectionUtils.convertDirectedListToMap(authnContexts);
        val mappedMethod = authnContextClassRefs.stream()
            .filter(ref -> StringUtils.isNotBlank(ref.getURI()))
            .filter(ref -> definedContexts.containsKey(ref.getURI()))
            .map(ref -> Pair.of(ref, definedContexts.get(ref.getURI())))
            .findFirst()
            .orElse(null);

        val attributes = assertion.getAttributes();
        val contextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
        if (attributes.containsKey(contextAttribute) && mappedMethod != null) {
            val authnContext = attributes.get(contextAttribute);
            val satisfiedContext = CollectionUtils.firstElement(authnContext)
                .map(Object::toString)
                .orElse(null);
            if (StringUtils.equals(mappedMethod.getValue(), satisfiedContext)) {
                return mappedMethod.getLeft().getURI();
            }
        }
        return null;
    }
}
