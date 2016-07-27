package org.apereo.cas.mgmt.config;

import org.apereo.cas.authorization.LdapAuthorizationGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchScope;
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
        final ConnectionFactory connectionFactory = Beans.newPooledConnectionFactory(casProperties.getLdapAuthz());
        final LdapAuthorizationGenerator gen = new LdapAuthorizationGenerator(connectionFactory,
                ldapAuthorizationGeneratorUserSearchExecutor());
        gen.setAllowMultipleResults(casProperties.getLdapAuthz().isAllowMultipleResults());
        gen.setRoleAttribute(casProperties.getLdapAuthz().getRoleAttribute());
        gen.setRolePrefix(casProperties.getLdapAuthz().getRolePrefix());
        return gen;
    }
    
    @RefreshScope
    @Bean
    public SearchExecutor ldapAuthorizationGeneratorUserSearchExecutor() {
        final SearchExecutor executor = new SearchExecutor();
        executor.setBaseDn(casProperties.getLdapAuthz().getBaseDn());
        executor.setSearchFilter(new SearchFilter(casProperties.getLdapAuthz().getSearchFilter()));
        executor.setReturnAttributes(ReturnAttributes.ALL.value());
        executor.setSearchScope(SearchScope.SUBTREE);
        return executor;
    }
}
