package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.webauthn.WebAuthnCredential;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link WebAuthnComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@AutoConfiguration
public class WebAuthnComponentSerializationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer webAuthnComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(WebAuthnCredential.class);
    }

}
