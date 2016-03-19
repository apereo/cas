package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This is {@link DefaultAuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component
@Qualifier("defaultAuthnContextClassRefBuilder")
public class DefaultAuthnContextClassRefBuilder implements AuthnContextClassRefBuilder {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String build(final Assertion assertion, final AuthnRequest authnRequest,
                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                        final SamlRegisteredService service) {
        final RequestedAuthnContext requestedAuthnContext = authnRequest.getRequestedAuthnContext();
        if (requestedAuthnContext == null) {
            logger.debug("No specific authN context is requested. Returning [{}]", AuthnContext.UNSPECIFIED_AUTHN_CTX);
            return AuthnContext.UNSPECIFIED_AUTHN_CTX;
        }
        final List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        if (authnContextClassRefs == null || authnContextClassRefs.isEmpty()) {
            logger.debug("Requested authN context class ref is unspecified. Returning [{}]", AuthnContext.UNSPECIFIED_AUTHN_CTX);
            return AuthnContext.UNSPECIFIED_AUTHN_CTX;
        }
        logger.debug("AuthN Context comparison is requested to use [{}]", requestedAuthnContext.getComparison());
        for (final AuthnContextClassRef authnContextClassRef : authnContextClassRefs) {
            logger.debug("Requested AuthN Context [{}]", authnContextClassRef.getAuthnContextClassRef());
        }
        if (StringUtils.isNotBlank(service.getRequiredAuthenticationContextClass())) {
            logger.debug("Using [{}] as indicated by SAML registered service [{}]",
                    service.getRequiredAuthenticationContextClass(),
                    service.getName());
            return service.getRequiredAuthenticationContextClass();
        }
        logger.debug("Returning default AuthN Context [{}]", AuthnContext.PPT_AUTHN_CTX);
        return AuthnContext.PPT_AUTHN_CTX;
    }
}
