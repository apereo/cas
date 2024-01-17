package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.geo.GeoLocationServiceConfigurer;
import org.apereo.cas.support.geo.azure.AzureMapsGeoLocationService;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasGeoLocationAzureMapsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GeoLocation, module = "azure")
@AutoConfiguration
public class CasGeoLocationAzureMapsAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "azureMapsGeoLocationService")
    public GeoLocationService azureMapsGeoLocationService(final CasConfigurationProperties casProperties) {
        return new AzureMapsGeoLocationService(casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "azureMapsGeoLocationServiceConfigurer")
    public GeoLocationServiceConfigurer azureMapsGeoLocationServiceConfigurer(
        @Qualifier("azureMapsGeoLocationService")
        final GeoLocationService azureMapsGeoLocationService) {
        return () -> azureMapsGeoLocationService;
    }
}
