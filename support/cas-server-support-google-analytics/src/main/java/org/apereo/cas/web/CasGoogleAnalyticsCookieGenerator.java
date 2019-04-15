package org.apereo.cas.web;

import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

/**
 * This is {@link CasGoogleAnalyticsCookieGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class CasGoogleAnalyticsCookieGenerator extends CookieRetrievingCookieGenerator {
    private static final long serialVersionUID = -812336828194851559L;

    public CasGoogleAnalyticsCookieGenerator(final CookieGenerationContext context) {
        super(context);
    }
}
