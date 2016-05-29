package org.apereo.cas.web.config;

import org.apereo.cas.web.WarningCookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;
import org.apereo.cas.web.support.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.TGCCookieRetrievingCookieGenerator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCookieConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCookieConfiguration")
public class CasCookieConfiguration {
    
    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator warnCookieGenerator() {
        return new WarningCookieRetrievingCookieGenerator();
    }
    
    @Bean
    public CookieValueManager defaultCookieValueManager() {
        return new DefaultCasCookieValueManager();
    }
    
    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator() {
        return new TGCCookieRetrievingCookieGenerator(defaultCookieValueManager());
    }
}
