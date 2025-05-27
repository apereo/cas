package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.DefaultCipherExecutorResolver;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import org.apereo.cas.web.support.mgmr.NoOpCookieValueManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreCookieAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@AutoConfiguration
public class CasCoreCookieAutoConfiguration {

    @Configuration(value = "CasCookieCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCookieCoreConfiguration {
        @ConditionalOnMissingBean(name = CookieValueManager.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CookieValueManager cookieValueManager(
            @Qualifier(GeoLocationService.BEAN_NAME)
            final ObjectProvider<GeoLocationService> geoLocationService,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties,
            @Qualifier("cookieCipherExecutor")
            final CipherExecutor cookieCipherExecutor) {
            if (casProperties.getTgc().getCrypto().isEnabled()) {
                val cipherExecutorResolver = new DefaultCipherExecutorResolver(cookieCipherExecutor, tenantExtractor,
                    TicketGrantingCookieProperties.class, bindingContext -> {
                    val properties = bindingContext.value();
                    return CipherExecutorUtils.newStringCipherExecutor(properties.getTgc().getCrypto(), TicketGrantingCookieCipherExecutor.class);
                });
                return new DefaultCasCookieValueManager(cipherExecutorResolver,
                    tenantExtractor, geoLocationService,
                    DefaultCookieSameSitePolicy.INSTANCE, casProperties.getTgc());
            }
            return new NoOpCookieValueManager(tenantExtractor);
        }

        @ConditionalOnMissingBean(name = "cookieCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CipherExecutor cookieCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getTgc().getCrypto();
            var enabled = crypto.isEnabled();
            if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey())
                && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
                LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration for cookie [{}], yet signing/encryption keys "
                    + "are defined for operations. CAS will proceed to enable the cookie encryption/signing functionality.", casProperties.getTgc().getName());
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
    static class CasCookieGeneratorConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasCookieBuilder.BEAN_NAME_WARN_COOKIE_BUILDER)
        public CasCookieBuilder warnCookieGenerator(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties) {
            val props = casProperties.getWarningCookie();
            return new CookieRetrievingCookieGenerator(
                CookieUtils.buildCookieGenerationContext(props),
                new NoOpCookieValueManager(tenantExtractor));
        }

        @ConditionalOnMissingBean(name = CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasCookieBuilder ticketGrantingTicketCookieGenerator(
            final CasConfigurationProperties casProperties,
            @Qualifier(CookieValueManager.BEAN_NAME)
            final CookieValueManager cookieValueManager) {
            val context = CookieUtils.buildCookieGenerationContext(casProperties.getTgc());
            return new CookieRetrievingCookieGenerator(context, cookieValueManager);
        }
    }

}
