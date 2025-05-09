package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.syncope.passwordless.SyncopePasswordlessUserAccountStore;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.List;

/**
 * This is {@link SyncopePasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn, module = "syncope")
@ConditionalOnClass(PasswordlessUserAccountStore.class)
@Configuration(value = "SyncopePasswordlessAuthenticationConfiguration", proxyBeanMethods = false)
class SyncopePasswordlessAuthenticationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "syncopePasswordlessUserAccountStore")
    public BeanSupplier<PasswordlessUserAccountStore> syncopePasswordlessUserAccountStore(
        final List<PasswordlessUserAccountCustomizer> customizerList,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(PasswordlessUserAccountStore.class)
            .when(BeanCondition.on("cas.authn.passwordless.accounts.syncope.url").isUrl().given(applicationContext))
            .supply(() -> new SyncopePasswordlessUserAccountStore(applicationContext, customizerList,
                casProperties.getAuthn().getPasswordless().getAccounts().getSyncope()))
            .otherwiseNull();
    }
}
