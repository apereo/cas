package org.apereo.cas.mgmt.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.webapp.mgmt.ManagementWebappProperties;
import org.apereo.cas.mgmt.authz.CasRoleBasedAuthorizer;
import org.apereo.cas.mgmt.authz.CasSpringSecurityAuthorizationGenerator;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.authorization.generator.FromAttributesAuthorizationGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasManagementAuthorizationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casManagementAuthorizationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasManagementAuthorizationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authorizationGenerator")
    @Bean
    @RefreshScope
    public AuthorizationGenerator authorizationGenerator() {
        final List<String> authzAttributes = casProperties.getMgmt().getAuthzAttributes();
        if (!authzAttributes.isEmpty()) {
            if (authzAttributes.stream().anyMatch(a -> a.equals("*"))) {
                return staticAdminRolesAuthorizationGenerator();
            }
            return new FromAttributesAuthorizationGenerator(authzAttributes.toArray(new String[]{}), new String[]{});
        }
        return springSecurityPropertiesAuthorizationGenerator();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "staticAdminRolesAuthorizationGenerator")
    public AuthorizationGenerator staticAdminRolesAuthorizationGenerator() {
        return (context, profile) -> {
            profile.addRoles(casProperties.getMgmt().getAdminRoles());
            return profile;
        };
    }

    @ConditionalOnMissingBean(name = "requireAnyRoleAuthorizer")
    @Bean
    @RefreshScope
    public Authorizer requireAnyRoleAuthorizer() {
        return new CasRoleBasedAuthorizer(casProperties.getMgmt().getAdminRoles());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "springSecurityPropertiesAuthorizationGenerator")
    public AuthorizationGenerator springSecurityPropertiesAuthorizationGenerator() {
        try {
            final ManagementWebappProperties mgmt = casProperties.getMgmt();
            return new CasSpringSecurityAuthorizationGenerator(mgmt.getUserPropertiesFile());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
