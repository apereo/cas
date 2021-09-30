package org.apereo.cas.support.geo.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.geo.maxmind.MaxmindDatabaseGeoLocationService;
import org.apereo.cas.util.ResourceUtils;

import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
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

    private static DatabaseReader readDatabase(final Resource maxmindDatabase) throws IOException {
        if (ResourceUtils.doesResourceExist(maxmindDatabase)) {
            return new DatabaseReader.Builder(maxmindDatabase.getFile()).fileMode(Reader.FileMode.MEMORY).withCache(new CHMCache()).build();
        }
        return null;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public GeoLocationService geoLocationService(final CasConfigurationProperties casProperties) throws Exception {
        val properties = casProperties.getMaxmind();
        val cityDatabase = readDatabase(properties.getCityDatabase());
        val countryDatabase = readDatabase(properties.getCountryDatabase());
        return new MaxmindDatabaseGeoLocationService(cityDatabase, countryDatabase);
    }
}
