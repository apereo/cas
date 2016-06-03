package org.apereo.cas.web.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Generates the tgc cookie.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TGCCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    /**
     * Instantiates a new TGC cookie retrieving cookie generator.
     *
     * @param casCookieValueManager the cas cookie value manager
     */
    public TGCCookieRetrievingCookieGenerator(final CookieValueManager casCookieValueManager) {
        super(casCookieValueManager);
    }
}
