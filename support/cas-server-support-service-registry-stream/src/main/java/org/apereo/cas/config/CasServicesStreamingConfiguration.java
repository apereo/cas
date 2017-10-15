package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.CasServicesRegistryStreamingEventListener;
import org.apereo.cas.services.publisher.CasRegisteredServiceNoOpStreamPublisher;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.StringBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasServicesStreamingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServicesStreamingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasServicesStreamingConfiguration {

    @Bean
    public CasServicesRegistryStreamingEventListener casServicesRegistryStreamingEventListener() {
        return new CasServicesRegistryStreamingEventListener(casRegisteredServiceStreamPublisher());
    }

    @ConditionalOnMissingBean(name = "casRegisteredServiceStreamPublisher")
    @Bean
    public CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher() {
        return new CasRegisteredServiceNoOpStreamPublisher();
    }

    @ConditionalOnMissingBean(name = "casRegisteredServiceStreamPublisherIdentifier")
    @Bean
    public StringBean casRegisteredServiceStreamPublisherIdentifier() {
        return new StringBean();     
    }
}
