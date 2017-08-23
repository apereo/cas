package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.saml.saml2.core.RequestAbstractType;

/**
 * This is {@link AuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface AuthnContextClassRefBuilder {

    /**
     * Gets authentication method from assertion.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @param service      the service
     * @return the authentication method from assertion
     */
    String build(Object assertion, RequestAbstractType authnRequest,
                 SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                 SamlRegisteredService service);
}
