package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.geo.GeoLocationServiceConfigurer;
import org.apereo.cas.support.geo.google.GoogleMapsGeoLocationService;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.maps.GaeRequestHandler;
import com.google.maps.GeoApiContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasGoogleMapsGeoCodingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GeoLocation, module = "googlemaps")
@AutoConfiguration
public class CasGoogleMapsGeoCodingAutoConfiguration {

    @ConditionalOnMissingBean(name = "googleMapsGeoLocationService")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public GeoLocationService googleMapsGeoLocationService(final CasConfigurationProperties casProperties) {
        val builder = new GeoApiContext.Builder();
        val properties = casProperties.getGeoLocation().getGoogleMaps();
        if (properties.isGoogleAppsEngine()) {
            builder.requestHandlerBuilder(new GaeRequestHandler.Builder());
        }
        if (StringUtils.isNotBlank(properties.getClientId()) && StringUtils.isNotBlank(properties.getClientSecret())) {
            builder.enterpriseCredentials(properties.getClientId(), properties.getClientSecret());
        }
        val context = builder.apiKey(properties.getApiKey())
            .connectTimeout(Beans.newDuration(properties.getConnectTimeout()).toMillis(), TimeUnit.MILLISECONDS)
            .build();
        return new GoogleMapsGeoLocationService(context);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleMapsGeoLocationServiceConfigurer")
    public GeoLocationServiceConfigurer googleMapsGeoLocationServiceConfigurer(
        @Qualifier("googleMapsGeoLocationService")
        final GeoLocationService googleMapsGeoLocationService) {
        return () -> googleMapsGeoLocationService;
    }
}
