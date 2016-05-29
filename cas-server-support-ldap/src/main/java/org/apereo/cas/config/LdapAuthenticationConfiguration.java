package org.apereo.cas.config;

import org.apereo.cas.authorization.generator.LdapAuthorizationGenerator;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link LdapAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapAuthenticationConfiguration")
public class LdapAuthenticationConfiguration {
    
    @Bean
    @RefreshScope
    public AuthorizationGenerator ldapAuthorizationGenerator() {
        return new LdapAuthorizationGenerator();
    }
}
