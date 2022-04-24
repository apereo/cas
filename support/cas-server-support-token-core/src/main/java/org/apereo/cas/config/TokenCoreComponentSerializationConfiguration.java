package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.token.authentication.TokenCredential;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link TokenCoreComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Tokens)
@AutoConfiguration
public class TokenCoreComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "tokenComponentSerializationPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ComponentSerializationPlanConfigurer tokenComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(TokenCredential.class);
    }
}
