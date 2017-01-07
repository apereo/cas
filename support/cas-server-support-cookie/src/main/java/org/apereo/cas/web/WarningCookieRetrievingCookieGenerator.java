package org.apereo.cas.web;

import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

/**
 * Generates the warning cookie.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WarningCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    public WarningCookieRetrievingCookieGenerator(final String name, final String path, final int maxAge, final boolean secure) {
        super(name, path, maxAge, secure, null);
    }
}
