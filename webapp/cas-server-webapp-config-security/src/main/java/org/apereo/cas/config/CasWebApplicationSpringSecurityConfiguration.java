package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.web.security.CasJdbcUserDetailsManagerConfigurer;
import org.apereo.cas.web.security.CasLdapUserDetailsManagerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.jaas.JaasAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;

/**
 * This is {@link CasWebApplicationSpringSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casWebApplicationSpringSecurityConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWebApplicationSpringSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void init(final AuthenticationManagerBuilder auth) throws Exception {
        if (StringUtils.isNotBlank(casProperties.getAdminPagesSecurity().getJdbc().getQuery())) {
            auth.apply(new CasJdbcUserDetailsManagerConfigurer(casProperties.getAdminPagesSecurity()));
        }
        if (isLdapAuthorizationActive()) {
            auth.apply(new CasLdapUserDetailsManagerConfigurer<>(casProperties.getAdminPagesSecurity()));
        }

        final AdminPagesSecurityProperties.Jaas jaas = casProperties.getAdminPagesSecurity().getJaas();
        if (jaas.getLoginConfig() != null) {
            final JaasAuthenticationProvider p = new JaasAuthenticationProvider();
            p.setLoginConfig(jaas.getLoginConfig());
            p.setLoginContextName(jaas.getLoginContextName());
            p.setRefreshConfigurationOnStartup(jaas.isRefreshConfigurationOnStartup());
            auth.authenticationProvider(p);
        }
    }

    private boolean isLdapAuthorizationActive() {
        final AdminPagesSecurityProperties.Ldap ldap = casProperties.getAdminPagesSecurity().getLdap();
        final LdapAuthorizationProperties authZ = ldap.getLdapAuthz();
        return StringUtils.isNotBlank(ldap.getBaseDn()) && StringUtils.isNotBlank(ldap.getLdapUrl())
                && StringUtils.isNotBlank(ldap.getUserFilter())
                && (StringUtils.isNotBlank(authZ.getRoleAttribute()) || StringUtils.isNotBlank(authZ.getGroupAttribute()));
    }
}
