package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.cookie.CookieSameSitePolicy;
import org.apereo.cas.web.cookie.CookieValueManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;

/**
 * This is {@link NoOpCookieValueManager}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Getter
public class NoOpCookieValueManager implements CookieValueManager {
    @Serial
    private static final long serialVersionUID = 5776311151053397600L;

    private final TenantExtractor tenantExtractor;

    @Override
    public String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        return givenCookieValue;
    }

    @Override
    public String obtainCookieValue(final String cookie, final HttpServletRequest request) {
        return cookie;
    }

    @Override
    public CookieSameSitePolicy getCookieSameSitePolicy() {
        return CookieSameSitePolicy.off();
    }
}
