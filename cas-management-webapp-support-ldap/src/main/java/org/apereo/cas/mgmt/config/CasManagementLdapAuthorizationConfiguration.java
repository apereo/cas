package org.apereo.cas.mgmt.config;

import org.pac4j.core.authorization.AuthorizationGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasManagementLdapAuthorizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casManagementLdapAuthorizationConfiguration")
@ComponentScan(basePackages = {"org.apereo.cas.authorization.generator"})
public class CasManagementLdapAuthorizationConfiguration {

    @Autowired
    @Qualifier("ldapAuthorizationGenerator")
    private AuthorizationGenerator authorizationGenerator;

    @Bean(name = "authorizationGenerator")
    public AuthorizationGenerator authorizationGenerator() {
        return this.authorizationGenerator;
    }
}
