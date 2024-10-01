package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.NoOpCasEventRepository;
import org.apereo.cas.support.events.listener.CasAuthenticationAuthenticationEventListener;
import org.apereo.cas.support.events.listener.CasAuthenticationEventListener;
import org.apereo.cas.support.events.web.CasEventsReportEndpoint;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.text.MessageSanitizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * This is {@link CasCoreEventsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Events)
@AutoConfiguration
public class CasCoreEventsAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.events.core.enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasCoreEventsListenerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreEventsListenerConfiguration {
        @ConditionalOnMissingBean(name = "defaultCasEventListener")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public CasAuthenticationEventListener defaultCasEventListener(
            @Qualifier(GeoLocationService.BEAN_NAME) final ObjectProvider<GeoLocationService> geoLocationService,
            @Qualifier(MessageSanitizer.BEAN_NAME) final MessageSanitizer messageSanitizer,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasEventRepository.BEAN_NAME) final CasEventRepository casEventRepository,
            @Qualifier(LogoutManager.DEFAULT_BEAN_NAME) final LogoutManager logoutManager) {
            return BeanSupplier.of(CasAuthenticationEventListener.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new CasAuthenticationAuthenticationEventListener(casEventRepository,
                    messageSanitizer, geoLocationService.getIfAvailable(), logoutManager))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasCoreEventsWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreEventsWebConfiguration {

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasEventsReportEndpoint casEventsReportEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new CasEventsReportEndpoint(casProperties, applicationContext);
        }
    }

    @Configuration(value = "CasCoreEventsRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreEventsRepositoryConfiguration {
        @ConditionalOnMissingBean(name = CasEventRepository.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasEventRepository casEventRepository(
            final ConfigurableApplicationContext applicationContext) throws Exception {
            return BeanSupplier.of(CasEventRepository.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> NoOpCasEventRepository.INSTANCE)
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "transactionManagerEvents")
        public PlatformTransactionManager transactionManagerEvents() {
            return new PseudoTransactionManager();
        }
    }

}
