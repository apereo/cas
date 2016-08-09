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
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleMapsGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleMapsGeoLocationService implements GeoLocationService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private CasConfigurationProperties casProperties;

    private final GeoApiContext context;
    
    public GoogleMapsGeoLocationService() {
        
        if (casProperties.getGoogleMaps().isGoogleAppsEngine()) {
            context = new GeoApiContext(new GaeRequestHandler());
        } else {
            context = new GeoApiContext();
        }
        if (StringUtils.isNotBlank(casProperties.getGoogleMaps().getClientId())
                && StringUtils.isNotBlank(casProperties.getGoogleMaps().getClientSecret())) {

            context.setEnterpriseCredentials(casProperties.getGoogleMaps().getClientId(),
                    casProperties.getGoogleMaps().getClientSecret());
        }
        context.setApiKey(casProperties.getGoogleMaps().getApiKey());
        context.setConnectTimeout(casProperties.getGoogleMaps().getConnectTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        return locate(address.getHostAddress());
    }

    @Override
    public GeoLocationResponse locate(final String address) {
        final Info info = UserInfo.getInfo(address);
        return locate(info.getPosition().getLatitude(), info.getPosition().getLongitude());
    }

    @Override
    public GeoLocationResponse locate(final double latitude, final double longitude) {
        final LatLng latlng = new LatLng(latitude, longitude);
        try {
            final GeocodingResult[] results = GeocodingApi.reverseGeocode(this.context, latlng).await();
            if (results != null && results.length > 0) {
                final GeocodingResult res = Arrays.stream(results).findFirst().get();
                
                return new GeoLocationResponse();
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
