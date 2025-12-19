package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAML)
@Configuration(value = "SamlAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class SamlAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = "samlAuthenticationMetaDataPopulator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator() {
        return new SamlAuthenticationMetaDataPopulator();
    }

    @ConditionalOnMissingBean(name = "samlAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer samlAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("samlAuthenticationMetaDataPopulator")
        final AuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator) {
        return plan -> plan.registerAuthenticationMetadataPopulator(samlAuthenticationMetaDataPopulator);
    }
}
