package org.apereo.cas.support.events.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.NoOpCasEventRepository;
import org.apereo.cas.support.events.listener.DefaultCasAuthenticationEventListener;
import org.apereo.cas.support.events.listener.DefaultLoggingCasEventListener;
import org.apereo.cas.support.events.web.CasEventsReportEndpoint;
import org.apereo.cas.util.spring.CasEventListener;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnCasFeatureModule;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreEventsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "CasCoreEventsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.Events)
public class CasCoreEventsConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.events.core.enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasCoreEventsListenerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreEventsListenerConfiguration {
        @ConditionalOnMissingBean(name = "defaultCasEventListener")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasEventListener defaultCasEventListener(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casEventRepository")
            final CasEventRepository casEventRepository) throws Exception {
            return BeanSupplier.of(CasEventListener.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultCasAuthenticationEventListener(casEventRepository))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "loggingCasEventListener")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasEventListener loggingCasEventListener() {
            return new DefaultLoggingCasEventListener();
        }
    }

    @Configuration(value = "CasCoreEventsWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreEventsWebConfiguration {

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasEventsReportEndpoint casEventsReportEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("casEventRepository")
            final CasEventRepository casEventRepository) {
            return new CasEventsReportEndpoint(casProperties, casEventRepository);
        }
    }

    @Configuration(value = "CasCoreEventsRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreEventsRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "casEventRepository")
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
    }

}
