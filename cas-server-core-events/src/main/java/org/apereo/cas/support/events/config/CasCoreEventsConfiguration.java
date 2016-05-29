package org.apereo.cas.support.events.config;

import org.apereo.cas.support.events.listener.DefaultCasEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreEventsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreEventsConfiguration")
public class CasCoreEventsConfiguration {
    
    @Bean
    public DefaultCasEventListener defaultCasEventListener() {
        return new DefaultCasEventListener();
    }
}
