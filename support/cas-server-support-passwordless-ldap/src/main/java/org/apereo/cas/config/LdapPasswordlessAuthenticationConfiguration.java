package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.account.LdapPasswordlessUserAccountStore;
import org.apereo.cas.util.LdapUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link LdapPasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "ldapPasswordlessAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapPasswordlessAuthenticationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public PasswordlessUserAccountStore passwordlessUserAccountStore(final CasConfigurationProperties casProperties) {
        val accounts = casProperties.getAuthn().getPasswordless().getAccounts();
        val ldap = accounts.getLdap();
        val connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
        return new LdapPasswordlessUserAccountStore(connectionFactory, ldap);
    }
}
