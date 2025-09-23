package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutorResolver;
import org.apereo.cas.web.cookie.CookieSameSitePolicy;
import org.apereo.cas.web.cookie.CookieValueManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
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
    @Serial
    private static final long serialVersionUID = 6362136147071376270L;

    private final CipherExecutorResolver cipherExecutorResolver;

    @Getter
    private final TenantExtractor tenantExtractor;

    @Getter
    private final CookieSameSitePolicy cookieSameSitePolicy;

    @Override
    public final String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        val cookieValue = buildCompoundCookieValue(givenCookieValue, request);
        LOGGER.trace("Encoding cookie value [{}]", cookieValue);
        return determineCipherExecutor(request).encode(cookieValue, ArrayUtils.EMPTY_OBJECT_ARRAY).toString();
    }

    @Override
    public String obtainCookieValue(final String cookie, final HttpServletRequest request) {
        val decoded = determineCipherExecutor(request).decode(cookie, ArrayUtils.EMPTY_OBJECT_ARRAY);
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

    protected String buildCompoundCookieValue(final String cookieValue, final HttpServletRequest request) {
        return cookieValue;
    }

    protected String obtainValueFromCompoundCookie(final String compoundValue, final HttpServletRequest request) {
        return compoundValue;
    }

    protected CipherExecutor<Serializable, Serializable> determineCipherExecutor(final HttpServletRequest request) {
        return cipherExecutorResolver.resolve(request);
    }
}
