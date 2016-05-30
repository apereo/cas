package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.support.TokenAuthenticationHandler;
import org.apereo.cas.web.flow.token.TokenAuthenticationAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link TokenAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("tokenAuthenticationConfiguration")
public class TokenAuthenticationConfiguration {
    
    @Bean
    public AuthenticationHandler tokenAuthenticationHandler() {
        return new TokenAuthenticationHandler();
    }
    
    @Bean
    public Action tokenAuthenticationAction() {
        return new TokenAuthenticationAction();
    }
}
