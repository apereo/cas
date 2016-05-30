package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.OpenIdWebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OpenIdWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("openIdWebflowConfiguration")
public class OpenIdWebflowConfiguration {
    
    @Bean
    public CasWebflowConfigurer openidWebflowConfigurer() {
        return new OpenIdWebflowConfigurer();
    }
}
