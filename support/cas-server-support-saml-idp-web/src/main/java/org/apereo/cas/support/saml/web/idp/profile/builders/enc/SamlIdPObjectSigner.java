package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlIdPObjectSigner}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface SamlIdPObjectSigner {
    /**
     * Default bean name.
     */
    String DEFAULT_BEAN_NAME = "samlObjectSigner";

    /**
     * Encode a given saml object by invoking a number of outbound security handlers on the context.
     *
     * @param <T>            the type parameter
     * @param samlObject     the saml object
     * @param service        the service
     * @param adaptor        the adaptor
     * @param response       the response
     * @param request        the request
     * @param binding        the binding
     * @param authnRequest   the authn request
     * @param messageContext the message context
     * @return the saml object
     * @throws Exception the saml exception
     */
    <T extends SAMLObject> T encode(T samlObject,
                                    SamlRegisteredService service,
                                    SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    HttpServletResponse response,
                                    HttpServletRequest request,
                                    String binding,
                                    RequestAbstractType authnRequest,
                                    MessageContext messageContext) throws Exception;

    /**
     * Gets saml idp metadata resolver.
     *
     * @return the saml id p metadata resolver
     */
    MetadataResolver getSamlIdPMetadataResolver();
}
