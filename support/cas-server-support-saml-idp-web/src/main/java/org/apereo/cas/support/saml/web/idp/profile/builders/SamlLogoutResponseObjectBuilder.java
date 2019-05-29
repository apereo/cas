package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlLogoutResponseObjectBuilder} defines the operations
 * required for building the saml2 logout response for an RP.
 *
 * @param <T> the type parameter
 * @author Krzysztof Zych
 * @since 6.1.0
 */
@FunctionalInterface
public interface SamlLogoutResponseObjectBuilder<T extends XMLObject> {

    /**
     * Build logout response.
     *
     * @param logoutRequest  the logout request
     * @param request        the http request
     * @param response       the http response
     * @param service        the saml service
     * @param adaptor        the service provider metadata adaptor
     * @param binding        the logout response binding
     * @param messageContext the message context
     * @return               the logout response
     * @throws SamlException the exception
     */
    T build(RequestAbstractType logoutRequest,
            HttpServletRequest request,
            HttpServletResponse response,
            SamlRegisteredService service,
            SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
            String binding,
            MessageContext messageContext) throws SamlException;
}
