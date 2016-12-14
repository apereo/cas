package org.apereo.cas.mgmt.config;

import org.apereo.cas.authorization.LdapAuthorizationGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasManagementLdapAuthorizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casManagementLdapAuthorizationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasManagementLdapAuthorizationConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public AuthorizationGenerator authorizationGenerator() {
        final LdapAuthorizationProperties ldapAuthz = casProperties.getMgmt().getLdapAuthz();
        final ConnectionFactory connectionFactory = Beans.newPooledConnectionFactory(ldapAuthz);
        return new LdapAuthorizationGenerator(connectionFactory, ldapAuthorizationGeneratorUserSearchExecutor(), ldapAuthz.isAllowMultipleResults(),
                ldapAuthz.getRoleAttribute(), ldapAuthz.getRolePrefix());
    }
    
    @RefreshScope
    @Bean
    public SearchExecutor ldapAuthorizationGeneratorUserSearchExecutor() {
        return Beans.newSearchExecutor(casProperties.getMgmt().getLdapAuthz().getBaseDn(),
                casProperties.getMgmt().getLdapAuthz().getSearchFilter());
    }
}
