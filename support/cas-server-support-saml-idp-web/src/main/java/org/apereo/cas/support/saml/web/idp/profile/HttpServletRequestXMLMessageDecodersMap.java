package org.apereo.cas.support.saml.web.idp.profile;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.springframework.http.HttpMethod;

import java.util.EnumMap;

/**
 * This is {@link HttpServletRequestXMLMessageDecodersMap}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class HttpServletRequestXMLMessageDecodersMap extends EnumMap<HttpMethod, BaseHttpServletRequestXMLMessageDecoder> {
    private static final long serialVersionUID = -461142665557954114L;

    public HttpServletRequestXMLMessageDecodersMap(final Class<HttpMethod> keyType) {
        super(keyType);
    }

    /**
     * Gets a cloned instance of the decoder.
     * Decoders are initialized once at configuration
     * and then re-created on demand so they can initialized
     * via OpenSAML again for new incoming requests.
     *
     * @param method the method
     * @return the instance
     */
    @SneakyThrows
    public BaseHttpServletRequestXMLMessageDecoder getInstance(final HttpMethod method) {
        val decoder = get(method);
        return (BaseHttpServletRequestXMLMessageDecoder) BeanUtils.cloneBean(decoder);
    }
}
