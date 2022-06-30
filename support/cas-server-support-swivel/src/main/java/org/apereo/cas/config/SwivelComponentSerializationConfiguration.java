package org.apereo.cas.config;

import org.apereo.cas.adaptors.swivel.SwivelTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SwivelComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 * @deprecated Since 6.6.
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "swivel")
@AutoConfiguration
@Deprecated(since = "6.6")
public class SwivelComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "swivelComponentSerializationPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer swivelComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(SwivelTokenCredential.class);
    }
}
