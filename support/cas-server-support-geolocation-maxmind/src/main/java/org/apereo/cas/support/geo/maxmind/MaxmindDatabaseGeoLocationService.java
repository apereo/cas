package org.apereo.cas.support.geo.maxmind;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.configuration.model.support.geo.maxmind.MaxmindProperties;
import org.apereo.cas.support.geo.AbstractGeoLocationService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

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
@AllArgsConstructor
@With
@SuppressWarnings("NullAway.Init")
public class MaxmindDatabaseGeoLocationService extends AbstractGeoLocationService {
    protected final MaxmindProperties properties;

    protected DatabaseReader cityDatabaseReader;

    protected DatabaseReader countryDatabaseReader;

    @Nullable
    protected WebServiceClient webServiceClient;

    @Override
    public @Nullable GeoLocationResponse locate(final InetAddress address) {
        try {
            val location = new GeoLocationResponse();
            FunctionUtils.doIfNotNull(cityDatabaseReader, _ -> {
                val response = cityDatabaseReader.city(address);
                location.addAddress(response.city().name());
                collectGeographicalPosition(location, response);
            });

            FunctionUtils.doIfNotNull(countryDatabaseReader, _ -> {
                val response = countryDatabaseReader.country(address);
                location.addAddress(response.country().name());
            });

            if (location.getAddresses().isEmpty() && properties.getAccountId() > 0 && StringUtils.isNotBlank(properties.getLicenseKey())) {
                val client = buildWebServiceClient();
                val cityResponse = client.city(address);
                location.addAddress(cityResponse.city().name());
                collectGeographicalPosition(location, cityResponse);

                val countryResponse = client.country(address);
                location.addAddress(countryResponse.country().name());
            }

            LOGGER.debug("Geo location for [{}] is calculated as [{}]", address, location);
            return location;
        } catch (final AddressNotFoundException e) {
            LOGGER.info(e.getMessage(), e);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public @Nullable GeoLocationResponse locate(final Double latitude, final Double longitude) {
        LOGGER.warn("Geo-locating an address by latitude/longitude [{}]/[{}] is not supported", latitude, longitude);
        return null;
    }

    protected WebServiceClient buildWebServiceClient() {
        return Optional.ofNullable(this.webServiceClient).orElseGet(
            () -> new WebServiceClient.Builder(properties.getAccountId(), properties.getLicenseKey())
                .host("geolite.info")
                .requestTimeout(Duration.ofSeconds(5))
                .proxy(ProxySelector.getDefault())
                .build());
    }

    private static void collectGeographicalPosition(final GeoLocationResponse location,
                                                    final CityResponse response) {
        val loc = response.location();
        if (loc != null) {
            if (loc.latitude() != null) {
                location.setLatitude(loc.latitude());
            }
            if (loc.longitude() != null) {
                location.setLongitude(loc.longitude());
            }
        }
    }
}
