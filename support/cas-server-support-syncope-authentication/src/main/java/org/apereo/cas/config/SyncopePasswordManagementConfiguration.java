package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.impl.NoOpPasswordManagementService;
import org.apereo.cas.syncope.pm.SyncopePasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SyncopePasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagement, module = "syncope")
@ConditionalOnClass(PasswordManagementService.class)
@Configuration(value = "SyncopePasswordManagementConfiguration", proxyBeanMethods = false)
class SyncopePasswordManagementConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "syncopePasswordChangeService")
    public PasswordManagementService passwordChangeService(
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordManagementCipherExecutor") final CipherExecutor passwordManagementCipherExecutor,
            @Qualifier(PasswordHistoryService.BEAN_NAME) final PasswordHistoryService passwordHistoryService) {
        val pm = casProperties.getAuthn().getPm();
        if (pm.getCore().isEnabled() && pm.getSyncope().getDomain() != null && pm.getSyncope().getUrl() != null) {
            return new SyncopePasswordManagementService(passwordManagementCipherExecutor, casProperties, passwordHistoryService);
        }
        return new NoOpPasswordManagementService(passwordManagementCipherExecutor, casProperties);
    }
}
