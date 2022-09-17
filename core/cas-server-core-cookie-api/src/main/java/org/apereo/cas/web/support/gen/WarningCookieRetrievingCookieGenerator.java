package org.apereo.cas.web.support.gen;

import org.apereo.cas.web.cookie.CookieGenerationContext;

import java.io.Serial;

/**
 * Generates the warning cookie.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WarningCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    @Serial
    private static final long serialVersionUID = 296771424248847492L;

    public WarningCookieRetrievingCookieGenerator(final CookieGenerationContext context) {
        super(context);
    }
}
