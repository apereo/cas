package org.apereo.cas.support.wsfederation.web;

import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;

/**
 * This is {@link WsFederationCookieGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WsFederationCookieGenerator extends CookieRetrievingCookieGenerator {
    private static final long serialVersionUID = -6908852892097058675L;

    public WsFederationCookieGenerator(final CookieValueManager valueManager,
                                       final String name, final String path, final int maxAge,
                                       final boolean secure, final String domain, final boolean httpOnly) {
        super(name, path, maxAge, secure, domain, httpOnly, valueManager);
    }
}
