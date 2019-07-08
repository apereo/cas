package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.cookie.CookieValueManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Provides basic encryption/decryption support for cookie values.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class EncryptedCookieValueManager implements CookieValueManager {
    private static final long serialVersionUID = 6362136147071376270L;
    /**
     * The cipher exec that is responsible for encryption and signing of the cookie.
     */
    private final transient CipherExecutor<Serializable, Serializable> cipherExecutor;

    @Override
    public final String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        val res = buildCompoundCookieValue(givenCookieValue, request);
        LOGGER.trace("Encoding cookie value [{}]", res);
        return cipherExecutor.encode(res, ArrayUtils.EMPTY_OBJECT_ARRAY).toString();
    }

    @Override
    public String obtainCookieValue(final String cookie, final HttpServletRequest request) {
        val decoded = cipherExecutor.decode(cookie, ArrayUtils.EMPTY_OBJECT_ARRAY);
        if (decoded == null) {
            LOGGER.trace("Could not decode cookie value [{}] for cookie", cookie);
            return null;
        }
        val cookieValue = decoded.toString();
        LOGGER.trace("Decoded cookie value is [{}]", cookieValue);
        if (StringUtils.isBlank(cookieValue)) {
            LOGGER.trace("Retrieved decoded cookie value is blank. Failed to decode cookie");
            return null;
        }

        return obtainValueFromCompoundCookie(cookieValue, request);
    }

    /**
     * Build the compound cookie value.
     *
     * @param cookieValue the raw cookie value that is being stored
     * @param request     the current web request
     * @return a compound cookie value that may contain additional data beyond the raw cookieValue
     */
    protected String buildCompoundCookieValue(final String cookieValue, final HttpServletRequest request) {
        return cookieValue;
    }

    /**
     * Obtain the cookie value from the compound cookie value.
     *
     * @param compoundValue The compound cookie value
     * @param request       the current web request
     * @return the original cookie value that was stored in the provided compound value.
     */
    protected String obtainValueFromCompoundCookie(final String compoundValue, final HttpServletRequest request) {
        return compoundValue;
    }
}
