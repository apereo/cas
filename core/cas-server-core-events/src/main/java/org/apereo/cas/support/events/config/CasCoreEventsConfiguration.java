package org.apereo.cas.support.events.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.NoOpCasEventRepository;
import org.apereo.cas.support.events.listener.DefaultCasEventListener;
import org.apereo.cas.support.events.listener.LoggingCasEventListener;
import org.apereo.cas.support.events.web.CasEventsReportEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreEventsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreEventsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.events", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasCoreEventsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "defaultCasEventListener")
    @Bean
    public DefaultCasEventListener defaultCasEventListener() {
        return new DefaultCasEventListener(casEventRepository());
    }

    @ConditionalOnMissingBean(name = "casEventRepository")
    @Bean
    public CasEventRepository casEventRepository() {
        return NoOpCasEventRepository.INSTANCE;
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public CasEventsReportEndpoint casEventsReportEndpoint() {
        return new CasEventsReportEndpoint(casProperties, casEventRepository());
    }

    @ConditionalOnMissingBean(name = "loggingCasEventListener")
    @Bean
    public LoggingCasEventListener loggingCasEventListener() {
        return new LoggingCasEventListener();
    }
}
