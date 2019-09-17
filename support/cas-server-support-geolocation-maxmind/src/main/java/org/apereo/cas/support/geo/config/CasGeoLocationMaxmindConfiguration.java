package org.apereo.cas.support.geo.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * This is {@link CasGeoLocationMaxmindConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casGeoLocationMaxmindConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasGeoLocationMaxmindConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @SneakyThrows
    public GeoLocationService geoLocationService() {
        val properties = casProperties.getMaxmind();
        val cityDatabase = readDatabase(properties.getCityDatabase());
        val countryDatabase = readDatabase(properties.getCountryDatabase());

        if (cityDatabase == null && countryDatabase == null) {
            throw new IllegalArgumentException("No geolocation services have been defined for Maxmind");
        }

        val svc = new MaxmindDatabaseGeoLocationService(cityDatabase, countryDatabase);
        svc.setIpStackAccessKey(properties.getIpStackApiAccessKey());
        return svc;
    }

    private static DatabaseReader readDatabase(final Resource maxmindDatabase) throws IOException {
        if (maxmindDatabase != null && maxmindDatabase.exists()) {
            return new DatabaseReader.Builder(maxmindDatabase.getFile()).withCache(new CHMCache()).build();
        }
        return null;
    }
}
