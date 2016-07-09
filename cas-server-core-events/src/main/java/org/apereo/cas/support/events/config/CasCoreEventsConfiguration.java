package org.apereo.cas.support.events.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.support.events.listener.DefaultCasEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CasCoreEventsConfiguration {

    @Autowired(required = false)
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;
    
    @Bean
    public DefaultCasEventListener defaultCasEventListener() {
        final DefaultCasEventListener l = new DefaultCasEventListener();
        l.setCasEventRepository(casEventRepository);
        return l;
    }
}
