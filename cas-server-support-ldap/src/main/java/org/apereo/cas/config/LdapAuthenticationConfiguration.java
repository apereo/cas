package org.apereo.cas.config;

import org.apereo.cas.authorization.generator.LdapAuthorizationGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapAuthenticationConfiguration {
    
    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorConnectionFactory")
    private ConnectionFactory connectionFactory;
    
    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorUserSearchExecutor")
    private SearchExecutor userSearchExecutor;
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    @RefreshScope
    public AuthorizationGenerator ldapAuthorizationGenerator() {
        final LdapAuthorizationGenerator gen = 
                new LdapAuthorizationGenerator(this.connectionFactory, this.userSearchExecutor);
        gen.setAllowMultipleResults(casProperties.getLdapAuthz().isAllowMultipleResults());
        gen.setRoleAttribute(casProperties.getLdapAuthz().getRoleAttribute());
        gen.setRolePrefix(casProperties.getLdapAuthz().getRolePrefix());
        
        return gen;
    }
}
