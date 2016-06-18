package org.apereo.cas.web.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.cookie.AbstractCookieProperties;
import org.apereo.cas.web.WarningCookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;
import org.apereo.cas.web.support.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.TGCCookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    private CasConfigurationProperties casProperties;


    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator warnCookieGenerator() {
        return configureCookieGenerator(new WarningCookieRetrievingCookieGenerator(),
                casProperties.getWarningCookie());
    }

    @Autowired
    @Bean
    public CookieValueManager defaultCookieValueManager(@Qualifier("cookieCipherExecutor")
                                                        final CipherExecutor cookieCipherExecutor) {
        return new DefaultCasCookieValueManager(cookieCipherExecutor);
    }

    @Autowired
    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator(@Qualifier("cookieCipherExecutor")
                                                                               final CipherExecutor cookieCipherExecutor) {
        final CookieRetrievingCookieGenerator bean =
                configureCookieGenerator(new TGCCookieRetrievingCookieGenerator(
                                defaultCookieValueManager(cookieCipherExecutor)),
                        casProperties.getTgc());
        bean.setCookieDomain(casProperties.getTgc().getDomain());
        bean.setRememberMeMaxAge(casProperties.getTgc().getRememberMeMaxAge());
        return bean;
    }

    /**
     * Configure.
     *
     * @param cookieGenerator cookie gen
     * @param props           props
     * @return cookie gen
     */
    private static CookieRetrievingCookieGenerator configureCookieGenerator(final CookieRetrievingCookieGenerator cookieGenerator,
                                                                            final AbstractCookieProperties props) {

        cookieGenerator.setCookieName(props.getName());
        cookieGenerator.setCookiePath(props.getPath());
        cookieGenerator.setCookieMaxAge(props.getMaxAge());
        cookieGenerator.setCookieSecure(props.isSecure());
        return cookieGenerator;
    }
}
