package org.apereo.cas.authentication.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoAuthenticationHandler;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasMongoAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casMongoAuthenticationConfiguration")
public class CasMongoAuthenticationConfiguration {
    
    @Bean
    @RefreshScope
    public AuthenticationHandler mongoAuthenticationHandler() {
        return new MongoAuthenticationHandler();
    }
}
