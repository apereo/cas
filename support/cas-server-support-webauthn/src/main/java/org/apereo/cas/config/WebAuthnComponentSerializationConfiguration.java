package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.webauthn.WebAuthnCredential;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link WebAuthnComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@Configuration(value = "WebAuthnComponentSerializationConfiguration", proxyBeanMethods = false)
class WebAuthnComponentSerializationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer webAuthnComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(WebAuthnCredential.class);
    }

}
