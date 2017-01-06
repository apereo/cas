package org.apereo.cas.support.geo.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.geo.google.GoogleMapsGeoLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link GoogleMapsGeoCodingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casGeoLocationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GoogleMapsGeoCodingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "geoLocationService")
    @Bean
    @RefreshScope
    public GeoLocationService geoLocationService() {
        return new GoogleMapsGeoLocationService(casProperties.getGoogleMaps());
    }
}
