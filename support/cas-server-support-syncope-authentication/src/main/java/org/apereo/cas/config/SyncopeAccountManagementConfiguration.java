package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.acct.provision.AccountRegistrationProvisionerConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.syncope.SyncopeAccountRegistrationProvisioner;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SyncopeAccountManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ConditionalOnClass(AccountRegistrationProvisionerConfigurer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountRegistration, module = "syncope")
@Configuration(value = "SyncopeAccountManagementConfiguration", proxyBeanMethods = false)
class SyncopeAccountManagementConfiguration {
    @ConditionalOnMissingBean(name = "syncopeAccountRegistrationProvisionerConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AccountRegistrationProvisionerConfigurer syncopeAccountRegistrationProvisionerConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(AccountRegistrationProvisionerConfigurer.class)
            .when(BeanCondition.on("cas.account-registration.provisioning.syncope.url")
                .isUrl().given(applicationContext.getEnvironment()))
            .supply(() -> () -> {
                val syncope = casProperties.getAccountRegistration().getProvisioning().getSyncope();
                return new SyncopeAccountRegistrationProvisioner(syncope);
            })
            .otherwiseProxy()
            .get();
    }
}
