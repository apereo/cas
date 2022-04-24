package org.apereo.cas.config.authentication.support;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.SAML)
@AutoConfiguration
public class SamlAuthenticationEventExecutionPlanConfiguration {

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
