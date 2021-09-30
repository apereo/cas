package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.gen.TicketGrantingCookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.gen.WarningCookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.NoOpCookieValueManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCookieConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCookieConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCookieConfiguration {

    @Configuration(value = "CasCookieCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCookieCoreConfiguration {
        @ConditionalOnMissingBean(name = "cookieValueManager")
        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CookieValueManager cookieValueManager(
            final CasConfigurationProperties casProperties,
            @Qualifier("cookieCipherExecutor")
            final CipherExecutor cookieCipherExecutor) {
            if (casProperties.getTgc().getCrypto().isEnabled()) {
                return new DefaultCasCookieValueManager(cookieCipherExecutor, casProperties.getTgc());
            }
            return NoOpCookieValueManager.INSTANCE;
        }

        @ConditionalOnMissingBean(name = "cookieCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public CipherExecutor cookieCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getTgc().getCrypto();
            var enabled = crypto.isEnabled();
            if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
                LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
                            + "are defined for operations. CAS will proceed to enable the cookie encryption/signing functionality.");
                enabled = true;
            }

            if (enabled) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, TicketGrantingCookieCipherExecutor.class);
            }

            LOGGER.warn("Ticket-granting cookie encryption/signing is turned off. This "
                        + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
                        + "signing and verification of ticket-granting cookies.");
            return CipherExecutor.noOp();
        }
    }

    @Configuration(value = "CasCookieGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCookieGeneratorConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "warnCookieGenerator")
        @Autowired
        public CasCookieBuilder warnCookieGenerator(final CasConfigurationProperties casProperties) {
            val props = casProperties.getWarningCookie();
            return new WarningCookieRetrievingCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
        }

        @ConditionalOnMissingBean(name = "ticketGrantingTicketCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasCookieBuilder ticketGrantingTicketCookieGenerator(
            final CasConfigurationProperties casProperties,
            @Qualifier("cookieValueManager")
            final CookieValueManager cookieValueManager) {
            val context = CookieUtils.buildCookieGenerationContext(casProperties.getTgc());
            return new TicketGrantingCookieRetrievingCookieGenerator(context, cookieValueManager);
        }
    }

}
