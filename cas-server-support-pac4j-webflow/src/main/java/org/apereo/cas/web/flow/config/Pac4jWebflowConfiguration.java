package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.Pac4jWebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link Pac4jWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("pac4jWebflowConfiguration")
public class Pac4jWebflowConfiguration {
    
    @Bean
    public CasWebflowConfigurer pac4jWebflowConfigurer() {
        return new Pac4jWebflowConfigurer();
    }
}
