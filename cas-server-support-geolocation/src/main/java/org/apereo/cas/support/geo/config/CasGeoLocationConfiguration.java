package org.apereo.cas.support.geo.config;

import org.apereo.cas.support.geo.GeoLocationService;
import org.apereo.cas.support.geo.maxmind.MaxmindDatabaseGeoLocationService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * This is {@link CasGeoLocationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casGeoLocationConfiguration")
public class CasGeoLocationConfiguration {
    
    @Bean
    @RefreshScope
    public GeoLocationService maxmindDatabaseGeoLocationService() {
        return new MaxmindDatabaseGeoLocationService();
    }
}
