package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.impl.account.LdapPasswordlessUserAccountStore;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnCasFeatureModule;

import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link LdapPasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "LdapPasswordlessAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn, module = "ldap")
public class LdapPasswordlessAuthenticationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordlessUserAccountStore passwordlessUserAccountStore(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(PasswordlessUserAccountStore.class)
            .when(BeanCondition.on("cas.authn.passwordless.accounts.ldap.ldap-url").given(applicationContext.getEnvironment()))
            .supply(() -> {
                val accounts = casProperties.getAuthn().getPasswordless().getAccounts();
                val ldap = accounts.getLdap();
                val connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
                return new LdapPasswordlessUserAccountStore(connectionFactory, ldap);
            })
            .otherwiseProxy()
            .get();
    }
}
