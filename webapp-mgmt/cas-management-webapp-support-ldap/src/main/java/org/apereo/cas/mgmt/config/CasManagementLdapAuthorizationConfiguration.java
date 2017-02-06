package org.apereo.cas.mgmt.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authorization.LdapUserAttributesToRolesAuthorizationGenerator;
import org.apereo.cas.authorization.LdapUserGroupsToRolesAuthorizationGenerator;
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

import java.util.Arrays;
import java.util.Collections;

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
        final LdapAuthorizationProperties ldapAuthz = casProperties.getMgmt().getLdap().getLdapAuthz();
        final ConnectionFactory connectionFactory = Beans.newLdaptivePooledConnectionFactory(casProperties.getMgmt().getLdap());

        if (StringUtils.isNotBlank(ldapAuthz.getGroupFilter()) && StringUtils.isNotBlank(ldapAuthz.getGroupAttribute())) {
            return new LdapUserGroupsToRolesAuthorizationGenerator(connectionFactory,
                    ldapAuthorizationGeneratorUserSearchExecutor(),
                    ldapAuthz.isAllowMultipleResults(),
                    ldapAuthz.getGroupAttribute(),
                    ldapAuthz.getGroupPrefix(),
                    ldapAuthorizationGeneratorGroupSearchExecutor());
        }
        return new LdapUserAttributesToRolesAuthorizationGenerator(connectionFactory,
                ldapAuthorizationGeneratorUserSearchExecutor(),
                ldapAuthz.isAllowMultipleResults(),
                ldapAuthz.getRoleAttribute(),
                ldapAuthz.getRolePrefix());
    }

    private SearchExecutor ldapAuthorizationGeneratorUserSearchExecutor() {
        final LdapAuthorizationProperties ldapAuthz = casProperties.getMgmt().getLdap().getLdapAuthz();
        return Beans.newLdaptiveSearchExecutor(ldapAuthz.getBaseDn(), ldapAuthz.getSearchFilter(),
                Collections.emptyList(), Arrays.asList(ldapAuthz.getRoleAttribute()));
    }

    private SearchExecutor ldapAuthorizationGeneratorGroupSearchExecutor() {
        final LdapAuthorizationProperties ldapAuthz = casProperties.getMgmt().getLdap().getLdapAuthz();
        return Beans.newLdaptiveSearchExecutor(ldapAuthz.getGroupBaseDn(), ldapAuthz.getGroupFilter(),
                Collections.emptyList(), Arrays.asList(ldapAuthz.getGroupAttribute()));
    }
}
