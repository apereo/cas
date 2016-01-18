package org.jasig.cas.web;

import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates the warning cookie.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("warnCookieGenerator")
public class WarningCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    @Override
    @Autowired
    public void setCookieName(@Value("${warn.cookie.name:CASPRIVACY}")
                                  final String cookieName) {
        super.setCookieName(cookieName);
    }

    @Override
    @Autowired
    public void setCookiePath(@Value("${warn.cookie.path:}")
                                  final String cookiePath) {
        super.setCookiePath(cookiePath);
    }

    @Override
    @Autowired
    public void setCookieMaxAge(@Value("${warn.cookie.maxAge:-1}")
                                    final Integer cookieMaxAge) {
        super.setCookieMaxAge(cookieMaxAge);
    }

    @Override
    @Autowired
    public void setCookieSecure(@Value("${warn.cookie.secure:true}")
                                    final boolean cookieSecure) {
        super.setCookieSecure(cookieSecure);
    }
}
