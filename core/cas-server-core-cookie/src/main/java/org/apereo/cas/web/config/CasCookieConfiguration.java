package org.apereo.cas.web.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.WarningCookieProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.apereo.cas.web.WarningCookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;
import org.apereo.cas.web.support.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.NoOpCookieValueManager;
import org.apereo.cas.web.support.TGCCookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCookieConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator warnCookieGenerator() {
        final WarningCookieProperties props = casProperties.getWarningCookie();
        return new WarningCookieRetrievingCookieGenerator(props.getName(), props.getPath(),
            props.getMaxAge(), props.isSecure(), props.isHttpOnly());
    }

    @ConditionalOnMissingBean(name = "cookieValueManager")
    @Bean
    public CookieValueManager cookieValueManager() {
        if (casProperties.getTgc().getCrypto().isEnabled()) {
            return new DefaultCasCookieValueManager(cookieCipherExecutor(), casProperties.getTgc());
        }
        return new NoOpCookieValueManager();
    }

    @ConditionalOnMissingBean(name = "cookieCipherExecutor")
    @RefreshScope
    @Bean
    public CipherExecutor cookieCipherExecutor() {
        final EncryptionJwtSigningJwtCryptographyProperties crypto = casProperties.getTgc().getCrypto();
        boolean enabled = crypto.isEnabled();
        if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
            LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
                + "are defined for operations. CAS will proceed to enable the cookie encryption/signing functionality.");
            enabled = true;
        }

        if (enabled) {
            return new TicketGrantingCookieCipherExecutor(crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(), crypto.getAlg());
        }

        LOGGER.warn("Ticket-granting cookie encryption/signing is turned off. This "
            + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
            + "signing and verification of ticket-granting cookies.");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "ticketGrantingTicketCookieGenerator")
    @Bean
    @RefreshScope
    public CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator() {
        final TicketGrantingCookieProperties tgc = casProperties.getTgc();
        final int rememberMeMaxAge = (int) Beans.newDuration(tgc.getRememberMeMaxAge()).getSeconds();
        return new TGCCookieRetrievingCookieGenerator(cookieValueManager(),
            tgc.getName(),
            tgc.getPath(),
            tgc.getDomain(),
            rememberMeMaxAge,
            tgc.isSecure(),
            tgc.getMaxAge(),
            tgc.isHttpOnly());
    }
}
