package org.apereo.cas.web.security;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authorization.LdapUserAttributesToRolesAuthorizationGenerator;
import org.apereo.cas.authorization.LdapUserGroupsToRolesAuthorizationGenerator;
import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.ldap.LdapAuthenticationProvider;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.ProviderManagerBuilder;

import java.util.ArrayList;

/**
 * This is {@link CasLdapUserDetailsManagerConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasLdapUserDetailsManagerConfigurer<B extends ProviderManagerBuilder<B>>
        extends SecurityConfigurerAdapter<AuthenticationManager, B> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasLdapUserDetailsManagerConfigurer.class);

    private final AdminPagesSecurityProperties adminPagesSecurityProperties;

    public CasLdapUserDetailsManagerConfigurer(final AdminPagesSecurityProperties securityProperties) {
        this.adminPagesSecurityProperties = securityProperties;
    }

    private AuthenticationProvider buildLdapAuthenticationProvider() {
        return new LdapAuthenticationProvider(build(), this.adminPagesSecurityProperties);
    }

    private AuthorizationGenerator<CommonProfile> build() {
        final LdapAuthorizationProperties ldapAuthz = adminPagesSecurityProperties.getLdap().getLdapAuthz();
        final ConnectionFactory connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(adminPagesSecurityProperties.getLdap());

        if (isGroupBasedAuthorization()) {
            LOGGER.debug("Handling LDAP authorization based on groups");
            return new LdapUserGroupsToRolesAuthorizationGenerator(connectionFactory,
                    ldapAuthorizationGeneratorUserSearchExecutor(),
                    ldapAuthz.isAllowMultipleResults(),
                    ldapAuthz.getGroupAttribute(),
                    ldapAuthz.getGroupPrefix(),
                    ldapAuthorizationGeneratorGroupSearchExecutor());
        }
        LOGGER.debug("Handling LDAP authorization based on attributes and roles");
        return new LdapUserAttributesToRolesAuthorizationGenerator(connectionFactory,
                ldapAuthorizationGeneratorUserSearchExecutor(),
                ldapAuthz.isAllowMultipleResults(),
                ldapAuthz.getRoleAttribute(),
                ldapAuthz.getRolePrefix());
    }

    private boolean isGroupBasedAuthorization() {
        final LdapAuthorizationProperties ldapAuthz = adminPagesSecurityProperties.getLdap().getLdapAuthz();
        return StringUtils.isNotBlank(ldapAuthz.getGroupFilter()) && StringUtils.isNotBlank(ldapAuthz.getGroupAttribute());
    }

    private SearchExecutor ldapAuthorizationGeneratorUserSearchExecutor() {
        final LdapAuthorizationProperties ldapAuthz = adminPagesSecurityProperties.getLdap().getLdapAuthz();
        return LdapUtils.newLdaptiveSearchExecutor(ldapAuthz.getBaseDn(), ldapAuthz.getSearchFilter(),
                new ArrayList<>(0), CollectionUtils.wrap(ldapAuthz.getRoleAttribute()));
    }

    private SearchExecutor ldapAuthorizationGeneratorGroupSearchExecutor() {
        final LdapAuthorizationProperties ldapAuthz = adminPagesSecurityProperties.getLdap().getLdapAuthz();
        return LdapUtils.newLdaptiveSearchExecutor(ldapAuthz.getGroupBaseDn(), ldapAuthz.getGroupFilter(),
                new ArrayList<>(0), CollectionUtils.wrap(ldapAuthz.getGroupAttribute()));
    }

    @Override
    public void configure(final B builder) {
        final AuthenticationProvider provider = postProcess(buildLdapAuthenticationProvider());
        builder.authenticationProvider(provider);
    }
}
