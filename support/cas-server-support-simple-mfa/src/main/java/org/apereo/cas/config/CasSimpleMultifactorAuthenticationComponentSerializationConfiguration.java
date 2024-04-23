package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSimpleMultifactorAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "simpleMfaComponentSerializationPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer simpleMfaComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(CasSimpleMultifactorTokenCredential.class);
    }
}
