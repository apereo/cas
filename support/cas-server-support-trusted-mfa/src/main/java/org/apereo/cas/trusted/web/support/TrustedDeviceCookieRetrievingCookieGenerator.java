package org.apereo.cas.trusted.web.support;

import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;

/**
 * {@link CookieRetrievingCookieGenerator} for trusted device cookies.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
public class TrustedDeviceCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {
    private static final long serialVersionUID = 3555244208199798618L;

    public TrustedDeviceCookieRetrievingCookieGenerator(final String name, final String path, final int maxAge,
                                                        final boolean secure, final String domain,
                                                        final boolean httpOnly,
                                                        final CookieValueManager cookieValueManager) {
        super(name, path, maxAge, secure, domain, httpOnly, cookieValueManager);
    }
}
