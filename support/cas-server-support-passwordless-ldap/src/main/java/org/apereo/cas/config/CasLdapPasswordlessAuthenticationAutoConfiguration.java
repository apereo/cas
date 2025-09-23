package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.impl.account.LdapPasswordlessUserAccountStore;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.List;

/**
 * This is {@link CasLdapPasswordlessAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn, module = "ldap")
@AutoConfiguration
public class CasLdapPasswordlessAuthenticationAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ldapPasswordlessUserAccountStore")
    public BeanSupplier<PasswordlessUserAccountStore> ldapPasswordlessUserAccountStore(
        final List<PasswordlessUserAccountCustomizer> customizerList,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(PasswordlessUserAccountStore.class)
            .when(BeanCondition.on("cas.authn.passwordless.accounts.ldap.ldap-url").given(applicationContext.getEnvironment()))
            .supply(() -> {
                val ldap = casProperties.getAuthn().getPasswordless().getAccounts().getLdap();
                val connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
                return new LdapPasswordlessUserAccountStore(new LdapConnectionFactory(connectionFactory),
                    ldap, applicationContext, customizerList);
            })
            .otherwiseNull();
    }
}
