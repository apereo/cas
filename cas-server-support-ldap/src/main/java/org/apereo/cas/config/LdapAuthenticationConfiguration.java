package org.apereo.cas.config;

import org.apereo.cas.authorization.generator.LdapAuthorizationGenerator;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    
    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorConnectionFactory")
    private ConnectionFactory connectionFactory;
    
    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorUserSearchExecutor")
    private SearchExecutor userSearchExecutor;

    
    @Autowired
    private LdapAuthorizationProperties properties;
    
    @Bean
    @RefreshScope
    public AuthorizationGenerator ldapAuthorizationGenerator() {
        final LdapAuthorizationGenerator gen = 
                new LdapAuthorizationGenerator(this.connectionFactory, this.userSearchExecutor);
        gen.setAllowMultipleResults(properties.isAllowMultipleResults());
        gen.setRoleAttribute(properties.getRoleAttribute());
        gen.setRolePrefix(properties.getRolePrefix());
        
        return gen;
    }
}
