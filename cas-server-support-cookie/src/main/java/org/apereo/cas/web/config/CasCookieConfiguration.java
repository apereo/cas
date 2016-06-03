package org.apereo.cas.web.config;

import org.apereo.cas.configuration.model.support.cookie.AbstractCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.WarningCookieProperties;
import org.apereo.cas.web.WarningCookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;
import org.apereo.cas.web.support.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.TGCCookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;

/**
 * This is {@link CasCookieConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCookieConfiguration")
@EnableConfigurationProperties({WarningCookieProperties.class, TicketGrantingCookieProperties.class})
public class CasCookieConfiguration {

    @Autowired
    WarningCookieProperties warningCookieProperties;

    @Autowired
    TicketGrantingCookieProperties tgcProperties;
    
    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator warnCookieGenerator() {
        return configureCookieGenerator(new WarningCookieRetrievingCookieGenerator(), this.warningCookieProperties);
    }
    
    @Bean
    public CookieValueManager defaultCookieValueManager() {
        return new DefaultCasCookieValueManager();
    }
    
    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator() {
        CookieRetrievingCookieGenerator bean =
                configureCookieGenerator(new TGCCookieRetrievingCookieGenerator(defaultCookieValueManager()), this.tgcProperties);
        bean.setCookieDomain(this.tgcProperties.getDomain());
        bean.setRememberMeMaxAge(this.tgcProperties.getRememberMeMaxAge());
        return bean;
    }

    /**
     * Configure.
     *
     * @param cookieGenerator cookie gen
     * @param props props
     * @return cookie gen
     */
    private CookieRetrievingCookieGenerator configureCookieGenerator(final CookieRetrievingCookieGenerator cookieGenerator,
                                                                     final AbstractCookieProperties props) {

        cookieGenerator.setCookieName(props.getName());
        cookieGenerator.setCookiePath(props.getPath());
        cookieGenerator.setCookieMaxAge(props.getMaxAge());
        cookieGenerator.setCookieSecure(props.isSecure());
        return cookieGenerator;
    }
}
