package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.saml.saml2.core.AuthnRequest;

/**
 * This is {@link AuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
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
    String build(Assertion assertion, AuthnRequest authnRequest, SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                 SamlRegisteredService service);
}
