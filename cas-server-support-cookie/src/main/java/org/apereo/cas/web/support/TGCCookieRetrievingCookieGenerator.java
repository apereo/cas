package org.apereo.cas.web.support;

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
