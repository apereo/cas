package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.WsFederationWebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link WsFederationAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("wsFederationAuthenticationWebflowConfiguration")
public class WsFederationAuthenticationWebflowConfiguration {
    
    @Bean
    public CasWebflowConfigurer wsFederationWebflowConfigurer() {
        return new WsFederationWebflowConfigurer();
    }
}
