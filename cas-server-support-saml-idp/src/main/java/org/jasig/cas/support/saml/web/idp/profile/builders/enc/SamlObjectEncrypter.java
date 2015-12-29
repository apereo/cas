package org.jasig.cas.support.saml.web.idp.profile.builders.enc;

import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlObjectEncrypter}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("samlObjectEncrypter")
public class SamlObjectEncrypter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Encode a given saml object by invoking a number of outbound security handlers on the context.
     *
     * @param <T>        the type parameter
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @param response   the response
     * @param request    the request
     * @return the t
     * @throws SamlException the saml exception
     */
    public final <T extends SAMLObject> T encode(final T samlObject,
                                                 final SamlRegisteredService service,
                                                 final SamlMetadataAdaptor adaptor,
                                                 final HttpServletResponse response,
                                                 final HttpServletRequest request) throws SamlException {
        try {
            logger.debug("Attempting to encrypt [{}] for [{}]", samlObject.getClass().getName(), adaptor.getEntityId());
            final MessageContext<T> outboundContext = new MessageContext<>();

            return samlObject;
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }
}
