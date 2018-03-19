package org.apereo.cas.web.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Provides basic encryption/decryption support for cookie values.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Slf4j
@AllArgsConstructor
public class EncryptedCookieValueManager implements CookieValueManager {
    /**
     * The cipher exec that is responsible for encryption and signing of the cookie.
     */
    private final CipherExecutor<Serializable, Serializable> cipherExecutor;

    @Override
    public final String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        final String res = buildValue(givenCookieValue, request);
        LOGGER.debug("Encoding cookie value [{}]", res);
        return cipherExecutor.encode(res).toString();
    }

    @Override
    public final String obtainCookieValue(final Cookie cookie, final HttpServletRequest request) {
        final String cookieValue = cipherExecutor.decode(cookie.getValue()).toString();
        LOGGER.debug("Decoded cookie value is [{}]", cookieValue);
        if (StringUtils.isBlank(cookieValue)) {
            LOGGER.debug("Retrieved decoded cookie value is blank. Failed to decode cookie [{}]", cookie.getName());
            return null;
        }

        return obtainValue(cookieValue, request);
    }

    protected String buildValue(final String givenCookieValue, final HttpServletRequest request) {
        return givenCookieValue;
    }

    protected String obtainValue(final String cookieValue, final HttpServletRequest request) {
        return cookieValue;
    }
}
