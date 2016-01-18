package org.jasig.cas.web.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates the tgc cookie.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("ticketGrantingTicketCookieGenerator")
public class TGCCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    /**
     * Instantiates a new TGC cookie retrieving cookie generator.
     *
     * @param casCookieValueManager the cas cookie value manager
     */
    @Autowired
    public TGCCookieRetrievingCookieGenerator(@Qualifier("defaultCookieValueManager")
        final CookieValueManager casCookieValueManager) {
        super(casCookieValueManager);
    }

    @Override
    @Autowired
    public void setCookieName(@Value("${tgc.name:TGC}")
                                  final String cookieName) {
        super.setCookieName(cookieName);
    }

    @Override
    @Autowired
    public void setCookiePath(@Value("${tgc.path:}")
                                  final String cookiePath) {
        super.setCookiePath(cookiePath);
    }

    @Override
    @Autowired
    public void setCookieMaxAge(@Value("${tgc.maxAge:-1}")
                                    final Integer cookieMaxAge) {
        super.setCookieMaxAge(cookieMaxAge);
    }

    @Override
    @Autowired
    public void setCookieSecure(@Value("${tgc.secure:true}")
                                    final boolean cookieSecure) {
        super.setCookieSecure(cookieSecure);
    }

    @Override
    @Autowired
    public void setRememberMeMaxAge(@Value("${tgc.remember.me.maxAge:1209600}")
                                final int max) {
        super.setRememberMeMaxAge(max);
    }
}
