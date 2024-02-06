package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DuoSecurityComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
@Configuration(value = "DuoSecurityComponentSerializationConfiguration", proxyBeanMethods = false)
class DuoSecurityComponentSerializationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "duoSecurityComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer duoSecurityComponentSerializationPlanConfigurer(
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(ComponentSerializationPlanConfigurer.class)
            .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> {
                plan.registerSerializableClass(DuoSecurityUniversalPromptCredential.class);
                plan.registerSerializableClass(DuoSecurityDirectCredential.class);
            })
            .otherwiseProxy()
            .get();
    }
}
