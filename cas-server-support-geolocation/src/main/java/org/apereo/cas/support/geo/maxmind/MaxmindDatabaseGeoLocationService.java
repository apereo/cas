package org.apereo.cas.support.geo.maxmind;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.geo.GeoLocation;
import org.apereo.cas.support.geo.GeoLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.InetAddress;

/**
 * This is {@link MaxmindDatabaseGeoLocationService} that reads geo data
 * from a maxmind database and constructs a geo location based on the ip address.
 * Default caching of the databases is enabled by default.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MaxmindDatabaseGeoLocationService implements GeoLocationService {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CasConfigurationProperties casProperties;

    private DatabaseReader cityDatabaseReader;
    private DatabaseReader countryDatabaseReader;

    /**
     * Init database readers.
     */
    @PostConstruct
    public void init() {
        try {
            if (casProperties.getMaxmindProperties().getCityDatabase().exists()) {
                this.cityDatabaseReader =
                        new DatabaseReader.Builder(casProperties.getMaxmindProperties().getCityDatabase().getFile())
                                .withCache(new CHMCache()).build();
            }

            if (casProperties.getMaxmindProperties().getCountryDatabase().exists()) {
                this.countryDatabaseReader =
                        new DatabaseReader.Builder(casProperties.getMaxmindProperties().getCountryDatabase().getFile())
                                .withCache(new CHMCache()).build();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GeoLocation locate(final InetAddress address) {
        final GeoLocation location = new GeoLocation();
        try {
            if (this.cityDatabaseReader != null) {
                final CityResponse response = this.cityDatabaseReader.city(address);
                location.setCity(response.getCity().getName());
            }
            if (this.countryDatabaseReader != null) {
                final CountryResponse response = this.countryDatabaseReader.country(address);
                location.setCountry(response.getCountry().getName());
            }
            logger.debug("Geo location for {} is calculated as {}", address, location);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return location;
    }
}
