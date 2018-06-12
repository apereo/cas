package org.apereo.cas.support.geo.config;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.geo.maxmind.MaxmindDatabaseGeoLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasGeoLocationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @SneakyThrows
    public GeoLocationService geoLocationService() {
        final var properties = casProperties.getMaxmind();

        final DatabaseReader cityDatabase;
        final DatabaseReader countryDatabase;

        if (properties.getCityDatabase().exists()) {
            cityDatabase = new DatabaseReader.Builder(properties.getCityDatabase().getFile()).withCache(new CHMCache()).build();
        } else {
            cityDatabase = null;
        }

        if (properties.getCountryDatabase().exists()) {
            countryDatabase = new DatabaseReader.Builder(properties.getCountryDatabase().getFile()).withCache(new CHMCache()).build();
        } else {
            countryDatabase = null;
        }

        if (cityDatabase == null && countryDatabase == null) {
            throw new IllegalArgumentException("No geolocation services have been defined for Maxmind");
        }

        final var svc = new MaxmindDatabaseGeoLocationService(cityDatabase, countryDatabase);
        svc.setIpStackAccessKey(properties.getIpStackApiAccessKey());
        return svc;
    }
}
