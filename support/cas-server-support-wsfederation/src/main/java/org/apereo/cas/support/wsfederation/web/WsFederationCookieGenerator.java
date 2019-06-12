package org.apereo.cas.support.wsfederation.web;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

/**
 * This is {@link WsFederationCookieGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WsFederationCookieGenerator extends CookieRetrievingCookieGenerator {
    private static final long serialVersionUID = -6908852892097058675L;

    public WsFederationCookieGenerator(final CookieGenerationContext context,
                                       final CookieValueManager casCookieValueManager) {
        super(context, casCookieValueManager);
    }

    public WsFederationCookieGenerator(final CookieValueManager defaultCasCookieValueManager,
                                       final CookieProperties cookie) {
        this(CookieUtils.buildCookieGenerationContext(cookie), defaultCasCookieValueManager);
    }
}
