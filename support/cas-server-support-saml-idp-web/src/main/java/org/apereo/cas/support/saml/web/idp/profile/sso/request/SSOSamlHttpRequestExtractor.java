package org.apereo.cas.support.saml.web.idp.profile.sso.request;

import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SignableSAMLObject;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link SSOSamlHttpRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface SSOSamlHttpRequestExtractor {
    /**
     * Extract the saml request from the http request. This can be an authentication request or a logout request.
     *
     * @param request the request
     * @param decoder the decoder
     * @param clazz   the clazz
     * @return the pair
     */
    Pair<? extends SignableSAMLObject, MessageContext> extract(HttpServletRequest request,
                                                               BaseHttpServletRequestXMLMessageDecoder decoder,
                                                               Class<? extends SignableSAMLObject> clazz);
}
