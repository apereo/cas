package org.apereo.cas.support.geo.azure;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.geo.AbstractGeoLocationService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.maps.geolocation.GeoLocationClient;
import com.azure.maps.geolocation.GeoLocationClientBuilder;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.ReverseSearchAddressOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.net.InetAddress;
import java.util.List;

/**
 * This is {@link AzureMapsGeoLocationService} that reads geo data
 * from Azure.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class AzureMapsGeoLocationService extends AbstractGeoLocationService {
    private final GeoLocationClient geoLocationClient;
    private final MapsSearchClient mapsSearchClient;

    public AzureMapsGeoLocationService(final CasConfigurationProperties casProperties) {
        val azure = casProperties.getGeoLocation().getAzure();
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val keyCredential = new AzureKeyCredential(resolver.resolve(azure.getSubscriptionId()));

        this.geoLocationClient = new GeoLocationClientBuilder()
            .credential(keyCredential)
            .clientId(resolver.resolve(azure.getClientId()))
            .buildClient();
        this.mapsSearchClient = new MapsSearchClientBuilder()
            .credential(keyCredential)
            .mapsClientId(azure.getClientId())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
    }

    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        return FunctionUtils.doUnchecked(() -> {
            val location = new GeoLocationResponse();
            val results = geoLocationClient.getLocation(address.getHostAddress());
            location.addAddress(results.getCountryRegion().getIsoCode());
            LOGGER.debug("Geo location for [{}] is calculated as [{}]", address, location);
            return location;
        });
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        val options = new ReverseSearchAddressOptions(new GeoPosition(longitude, latitude));
        val searchResult = mapsSearchClient.beginReverseSearchAddressBatch(List.of(options)).getFinalResult();
        val location = new GeoLocationResponse();
        for (val item : searchResult.getBatchItems()) {
            for (val result : item.getResult().getAddresses()) {
                location.addAddress(result.getAddress().getFreeformAddress());
                location.setLatitude(result.getPosition().getLatitude());
                location.setLongitude(result.getPosition().getLongitude());
            }
        }
        return location;
    }
}
