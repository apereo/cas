package org.apereo.cas.support.events.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.NoOpCasEventRepository;
import org.apereo.cas.support.events.listener.DefaultCasAuthenticationEventListener;
import org.apereo.cas.support.events.listener.DefaultLoggingCasEventListener;
import org.apereo.cas.support.events.web.CasEventsReportEndpoint;
import org.apereo.cas.util.spring.CasEventListener;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@ConditionalOnProperty(prefix = "cas.events.core", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasCoreEventsConfiguration {

    @Configuration(value = "CasCoreEventsListenerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreEventsListenerConfiguration {
        @ConditionalOnMissingBean(name = "defaultCasEventListener")
        @Bean
        public CasEventListener defaultCasEventListener(
            @Qualifier("casEventRepository")
            final CasEventRepository casEventRepository) {
            return new DefaultCasAuthenticationEventListener(casEventRepository);
        }

        @ConditionalOnMissingBean(name = "loggingCasEventListener")
        @Bean
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
        public CasEventRepository casEventRepository() {
            return NoOpCasEventRepository.INSTANCE;
        }
    }

}
