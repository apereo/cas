package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.consent.ConsentRepositoryBuilder;
import org.apereo.cas.consent.RestfulConsentRepository;
import org.apereo.cas.consent.TenantConsentRepositoryBuilder;
import org.apereo.cas.consent.TenantRestfulConsentRepositoryBuilder;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasConsentRestAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Consent, module = "rest")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasConsentRestAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "restfulConsentRepositoryBuilder")
    public ConsentRepositoryBuilder restfulConsentRepositoryBuilder(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
        final HttpClient httpClient,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(ConsentRepositoryBuilder.class)
            .when(BeanCondition.on("cas.consent.rest.url").given(applicationContext.getEnvironment()))
            .supply(() -> () -> new RestfulConsentRepository(casProperties.getConsent().getRest(), httpClient))
            .otherwiseProxy()
            .get();
    }

    @Configuration(value = "CasConsentRestMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentRestMultitenancyConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restfulConsentMultitenancyRepositoryBuilder")
        public TenantConsentRepositoryBuilder restfulConsentMultitenancyRepositoryBuilder(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient) {
            return BeanSupplier.of(TenantConsentRepositoryBuilder.class)
                .when(BeanCondition.on("cas.multitenancy.core.enabled").isTrue().given(applicationContext))
                .supply(() -> new TenantRestfulConsentRepositoryBuilder(httpClient))
                .otherwise(TenantConsentRepositoryBuilder::noOp)
                .get();
        }
    }
}
