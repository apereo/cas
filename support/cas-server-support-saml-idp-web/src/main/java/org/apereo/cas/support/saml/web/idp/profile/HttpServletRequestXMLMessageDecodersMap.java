package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.springframework.http.HttpMethod;

import java.io.Serial;
import java.util.EnumMap;

/**
 * This is {@link HttpServletRequestXMLMessageDecodersMap}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class HttpServletRequestXMLMessageDecodersMap extends EnumMap<HttpMethod, BaseHttpServletRequestXMLMessageDecoder> {
    @Serial
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
    public BaseHttpServletRequestXMLMessageDecoder getInstance(final HttpMethod method) {
        return FunctionUtils.doUnchecked(() -> {
            val decoder = get(method);
            return (BaseHttpServletRequestXMLMessageDecoder) BeanUtils.cloneBean(decoder);
        });
    }
}
