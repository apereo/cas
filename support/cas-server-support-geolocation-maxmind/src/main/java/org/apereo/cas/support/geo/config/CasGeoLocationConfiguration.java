package org.apereo.cas.support.geo.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.geo.maxmind.MaxmindProperties;
import org.apereo.cas.support.geo.maxmind.MaxmindDatabaseGeoLocationService;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * This is {@link CasGeoLocationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casGeoLocationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasGeoLocationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @SneakyThrows
    public GeoLocationService geoLocationService() {
        val properties = casProperties.getMaxmind();
        val cityDatabase = readCityDatabase(properties);
        val countryDatabase = readCountryDatabase(properties);

        if (cityDatabase == null && countryDatabase == null) {
            throw new IllegalArgumentException("No geolocation services have been defined for Maxmind");
        }

        val svc = new MaxmindDatabaseGeoLocationService(cityDatabase, countryDatabase);
        svc.setIpStackAccessKey(properties.getIpStackApiAccessKey());
        return svc;
    }

    private DatabaseReader readCountryDatabase(final MaxmindProperties properties) throws IOException {
        if (properties.getCountryDatabase().exists()) {
            return new DatabaseReader.Builder(properties.getCountryDatabase().getFile()).withCache(new CHMCache()).build();
        }
        return null;
    }

    private DatabaseReader readCityDatabase(final MaxmindProperties properties) throws IOException {
        if (properties.getCityDatabase().exists()) {
            return new DatabaseReader.Builder(properties.getCityDatabase().getFile()).withCache(new CHMCache()).build();
        }
        return null;
    }
}
