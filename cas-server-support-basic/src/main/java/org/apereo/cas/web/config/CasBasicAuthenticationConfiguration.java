package org.apereo.cas.web.config;

import org.apereo.cas.web.flow.BasicAuthenticationAction;
import org.apereo.cas.web.flow.BasicAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasBasicAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casBasicAuthenticationConfiguration")
public class CasBasicAuthenticationConfiguration {
    
    @Bean
    public Action basicAuthenticationAction() {
        return new BasicAuthenticationAction();
    }
    
    @Bean
    public CasWebflowConfigurer basicAuthenticationWebflowConfigurer() {
        return new BasicAuthenticationWebflowConfigurer();
    }
}
