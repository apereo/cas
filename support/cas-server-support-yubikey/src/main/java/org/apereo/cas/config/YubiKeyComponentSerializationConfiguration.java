package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link YubiKeyComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.YubiKey)
@Configuration(value = "YubiKeyComponentSerializationConfiguration", proxyBeanMethods = false)
class YubiKeyComponentSerializationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yubikeyComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer yubikeyComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(YubiKeyCredential.class);
    }
}
