package org.apereo.cas.support.saml.web.idp.profile;

import module java.base;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.springframework.http.HttpMethod;

/**
 * This is {@link XMLMessageDecodersMap}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface XMLMessageDecodersMap extends Map<HttpMethod, BaseHttpServletRequestXMLMessageDecoder> {
    /**
     * Gets instance.
     *
     * @param method the method
     * @return the instance
     */
    BaseHttpServletRequestXMLMessageDecoder getInstance(HttpMethod method);
}
