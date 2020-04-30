package org.apereo.cas.support.geo.maxmind;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.support.geo.AbstractGeoLocationService;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.InetAddress;

/**
 * This is {@link MaxmindDatabaseGeoLocationService} that reads geo data
 * from a maxmind database and constructs a geo location based on the ip address.
 * Default caching of the databases is enabled by default.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class MaxmindDatabaseGeoLocationService extends AbstractGeoLocationService {
    private final DatabaseReader cityDatabaseReader;

    private final DatabaseReader countryDatabaseReader;

    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        try {
            if (cityDatabaseReader == null && countryDatabaseReader == null) {
                throw new IllegalArgumentException("No geolocation services have been defined for Maxmind");
            }

            val location = new GeoLocationResponse();
            if (this.cityDatabaseReader != null) {
                val response = this.cityDatabaseReader.city(address);
                location.addAddress(response.getCity().getName());
                val loc = response.getLocation();
                if (loc != null) {
                    if (loc.getLatitude() != null) {
                        location.setLatitude(loc.getLatitude());
                    }
                    if (loc.getLongitude() != null) {
                        location.setLongitude(loc.getLongitude());
                    }
                }
            }
            if (this.countryDatabaseReader != null) {
                val response = this.countryDatabaseReader.country(address);
                location.addAddress(response.getCountry().getName());
            }
            LOGGER.debug("Geo location for [{}] is calculated as [{}]", address, location);
            return location;
        } catch (final AddressNotFoundException e) {
            LOGGER.info(e.getMessage(), e);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public GeoLocationResponse locate(final String address) {
        try {
            return locate(InetAddress.getByName(address));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        LOGGER.warn("Geo-locating an address by latitude/longitude [{}]/[{}] is not supported", latitude, longitude);
        return null;
    }
}
