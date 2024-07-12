package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.springframework.http.HttpMethod;

import java.io.Serial;
import java.util.HashMap;

/**
 * This is {@link HttpServletRequestXMLMessageDecodersMap}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class HttpServletRequestXMLMessageDecodersMap extends HashMap<HttpMethod, BaseHttpServletRequestXMLMessageDecoder> implements XMLMessageDecodersMap {
    @Serial
    private static final long serialVersionUID = -461142665557954114L;

    @Override
    public BaseHttpServletRequestXMLMessageDecoder getInstance(final HttpMethod method) {
        return FunctionUtils.doUnchecked(() -> {
            val decoder = get(method);
            return (BaseHttpServletRequestXMLMessageDecoder) BeanUtils.cloneBean(decoder);
        });
    }
}
