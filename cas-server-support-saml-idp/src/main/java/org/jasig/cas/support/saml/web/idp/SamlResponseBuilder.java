package org.jasig.cas.support.saml.web.idp;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlResponseBuilder} defines the operations
 * required for building the saml response for an RP.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface SamlResponseBuilder {

    /**
     * Build response.
     *
     * @param authnRequest the authn request
     * @param request      the request
     * @param response     the response
     * @param assertion    the assertion
     * @param service      the service
     * @param adaptor      the adaptor
     * @return the response
     * @throws Exception the exception
     */
    Response build(final AuthnRequest authnRequest, final HttpServletRequest request,
                   final HttpServletResponse response, final Assertion assertion,
                   final SamlRegisteredService service, final SamlMetadataAdaptor adaptor)
                    throws Exception;
}
