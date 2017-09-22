package org.apereo.cas.support.geo.google;

import com.google.maps.GaeRequestHandler;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import io.userinfo.client.UserInfo;
import io.userinfo.client.model.Info;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.configuration.model.support.geo.googlemaps.GoogleMapsProperties;
import org.apereo.cas.support.geo.AbstractGeoLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleMapsGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleMapsGeoLocationService extends AbstractGeoLocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleMapsGeoLocationService.class);

    private final GeoApiContext context;

    public GoogleMapsGeoLocationService(final GoogleMapsProperties properties) {
        if (properties.isGoogleAppsEngine()) {
            context = new GeoApiContext(new GaeRequestHandler());
        } else {
            context = new GeoApiContext();
        }
        if (StringUtils.isNotBlank(properties.getClientId()) && StringUtils.isNotBlank(properties.getClientSecret())) {
            context.setEnterpriseCredentials(properties.getClientId(), properties.getClientSecret());
        }
        context.setApiKey(properties.getApiKey());
        context.setConnectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS);
    }
    
    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        return locate(address.getHostAddress());
    }

    @Override
    public GeoLocationResponse locate(final String address) {
        final Info info = UserInfo.getInfo(address);
        if (info != null && info.getPosition() != null) {
            return locate(info.getPosition().getLatitude(), info.getPosition().getLongitude());
        }
        return null;
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        if (latitude == null || longitude == null) {
            LOGGER.debug("latitude/longitude must not be null in order for geolocation to proceed");
            return null;
        }

        final GeoLocationResponse r = new GeoLocationResponse();
        r.setLatitude(latitude);
        r.setLongitude(longitude);

        final LatLng latlng = new LatLng(latitude, longitude);
        try {
            final GeocodingResult[] results = GeocodingApi.reverseGeocode(this.context, latlng).await();
            if (results != null && results.length > 0) {
                Arrays.stream(results)
                        .map(result -> result.formattedAddress)
                        .forEach(r::addAddress);

                return r;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return r;
    }
}
