package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.geo.GeoLocationServiceConfigurer;
import org.apereo.cas.support.geo.ip.IPGeoLocationService;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import io.ipgeolocation.sdk.api.IPGeolocationAPI;
import io.ipgeolocation.sdk.invoker.ApiClient;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasIPGeoLocationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GeoLocation, module = "ip")
@AutoConfiguration
public class CasIPGeoLocationAutoConfiguration {

    @ConditionalOnMissingBean(name = "ipGeoLocationService")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public GeoLocationService ipGeoLocationService(final CasConfigurationProperties casProperties) {
        val key = casProperties.getGeoLocation().getIpGeoLocation().getApiKey();
        val apiClient = new ApiClient();
        apiClient.setApiKey(key);
        return new IPGeoLocationService(new IPGeolocationAPI(apiClient));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ipGeoLocationServiceConfigurer")
    public GeoLocationServiceConfigurer ipGeoLocationServiceConfigurer(
        @Qualifier("ipGeoLocationService")
        final GeoLocationService ipGeoLocationService) {
        return () -> ipGeoLocationService;
    }
}
