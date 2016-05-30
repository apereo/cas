package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.StormpathAuthenticationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link StormpathAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("stormpathAuthenticationConfiguration")
public class StormpathAuthenticationConfiguration {
    
    @Bean
    public AuthenticationHandler stormpathAuthenticationHandler() {
        return new StormpathAuthenticationHandler();
    }
}
