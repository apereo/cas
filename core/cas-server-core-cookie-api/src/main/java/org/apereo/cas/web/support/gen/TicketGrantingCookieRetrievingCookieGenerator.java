package org.apereo.cas.web.support.gen;

import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.cookie.CookieValueManager;

/**
 * Generates the tgc cookie.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TicketGrantingCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    private static final long serialVersionUID = -1239028220717183717L;

    public TicketGrantingCookieRetrievingCookieGenerator(final CookieGenerationContext context,
                                                         final CookieValueManager casCookieValueManager) {
        super(context, casCookieValueManager);
    }
}
