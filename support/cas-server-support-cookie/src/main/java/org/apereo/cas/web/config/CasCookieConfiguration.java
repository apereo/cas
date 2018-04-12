package org.apereo.cas.web.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.WarningCookieProperties;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.apereo.cas.web.WarningCookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;
import org.apereo.cas.web.support.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.NoOpCookieValueManager;
import org.apereo.cas.web.support.TGCCookieRetrievingCookieGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CasCookieConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCookieConfiguration.class);

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
    @Autowired
    @Bean
    public CookieValueManager cookieValueManager(@Qualifier("cookieCipherExecutor") final CipherExecutor cipherExecutor) {
        if (casProperties.getTgc().getCrypto().isEnabled()) {
            return new DefaultCasCookieValueManager(cipherExecutor);
        }
        return new NoOpCookieValueManager();
    }

    @ConditionalOnMissingBean(name = "cookieCipherExecutor")
    @RefreshScope
    @Bean
    public CipherExecutor cookieCipherExecutor() {
        final EncryptionJwtSigningJwtCryptographyProperties crypto = casProperties.getTgc().getCrypto();
        boolean enabled = crypto.isEnabled();
        if (!enabled && (StringUtils.isNotBlank(crypto.getEncryption().getKey())) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
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
        return NoOpCipherExecutor.getInstance();
    }

    @Autowired
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "ticketGrantingTicketCookieGenerator")
    public CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator(@Qualifier("cookieCipherExecutor") final CipherExecutor cipherExecutor) {
        final TicketGrantingCookieProperties tgc = casProperties.getTgc();
        final int rememberMeMaxAge = (int) tgc.getRememberMeMaxAge();
        return new TGCCookieRetrievingCookieGenerator(cookieValueManager(cipherExecutor),
                tgc.getName(),
                tgc.getPath(), tgc.getDomain(),
                rememberMeMaxAge, tgc.isSecure(),
                tgc.getMaxAge(),
                tgc.isHttpOnly());
    }
}
