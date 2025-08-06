package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.rest.CasConfigurationMetadataServerEndpoint;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreConfigurationMetadataAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration)
@AutoConfiguration
public class CasCoreConfigurationMetadataAutoConfiguration {
    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasConfigurationMetadataServerEndpoint configurationMetadataServerEndpoint(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasConfigurationMetadataRepository.BEAN_NAME)
        final CasConfigurationMetadataRepository casConfigurationMetadataRepository,
        final CasConfigurationProperties casProperties) {
        return new CasConfigurationMetadataServerEndpoint(casProperties,
            applicationContext, casConfigurationMetadataRepository);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasConfigurationMetadataRepository.BEAN_NAME)
    public CasConfigurationMetadataRepository casConfigurationMetadataRepository() {
        return new CasConfigurationMetadataRepository();
    }
}
