package org.apereo.cas.mgmt.config;

import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@ComponentScan(basePackages = {"org.apereo.cas.authorization"})
public class CasManagementLdapAuthorizationConfiguration {

    @Value("${ldap.baseDn:}")
    private String baseDn;

    @Value("${ldap.user.searchFilter:}")
    private String searchFilter;

    @Autowired
    @Qualifier("ldapAuthorizationGenerator")
    private AuthorizationGenerator authorizationGenerator;

    /**
     * Authorization generator for ldap access.
     *
     * @return the authorization generator
     */
    @RefreshScope
    @Bean(name = "authorizationGenerator")
    public AuthorizationGenerator authorizationGenerator() {
        return this.authorizationGenerator;
    }

    /**
     * Ldap authorization search executor.
     *
     * @return the search executor
     */
    @RefreshScope
    @Bean(name = "ldapAuthorizationGeneratorUserSearchExecutor")
    public SearchExecutor ldapAuthorizationGeneratorUserSearchExecutor() {
        final SearchExecutor executor = new SearchExecutor();
        executor.setBaseDn(this.baseDn);
        executor.setSearchFilter(new SearchFilter(this.searchFilter));
        executor.setReturnAttributes(ReturnAttributes.ALL.value());
        return executor;
    }
}
