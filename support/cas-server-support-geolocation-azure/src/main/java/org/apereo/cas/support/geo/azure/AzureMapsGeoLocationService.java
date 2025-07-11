package org.apereo.cas.support.geo.azure;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.geo.AbstractGeoLocationService;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.geolocation.GeolocationClient;
import com.azure.maps.geolocation.GeolocationClientBuilder;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.implementation.models.ReverseGeocodingResultTypeEnum;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    private final GeolocationClient geoLocationClient;
    private final MapsSearchClient mapsSearchClient;

    public AzureMapsGeoLocationService(final CasConfigurationProperties casProperties) {
        val keyCredential = buildAzureCredentials(casProperties);
        this.geoLocationClient = buildGeoLocationClient(casProperties, keyCredential);
        this.mapsSearchClient = buildMapSearchClient(casProperties, keyCredential);
    }

    private static Object buildAzureCredentials(final CasConfigurationProperties casProperties) {
        val azure = casProperties.getGeoLocation().getAzure();
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        return StringUtils.isNotBlank(azure.getApiKey())
            ? new AzureKeyCredential(resolver.resolve(azure.getApiKey()))
            : new DefaultAzureCredentialBuilder()
            .tenantId(resolver.resolve(azure.getTenantId()))
            .managedIdentityClientId(resolver.resolve(azure.getClientId()))
            .build();
    }

    private static GeolocationClient buildGeoLocationClient(final CasConfigurationProperties casProperties,
                                                            final Object keyCredential) {
        val azure = casProperties.getGeoLocation().getAzure();
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val builder = new GeolocationClientBuilder();
        if (keyCredential instanceof final AzureKeyCredential azkc) {
            builder.credential(azkc);
        } else {
            builder.credential((TokenCredential) keyCredential);
        }
        return builder.clientId(resolver.resolve(azure.getClientId())).buildClient();
    }

    private static MapsSearchClient buildMapSearchClient(final CasConfigurationProperties casProperties,
                                                         final Object keyCredential) {
        val azure = casProperties.getGeoLocation().getAzure();
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val builder = new MapsSearchClientBuilder();
        if (keyCredential instanceof final AzureKeyCredential azkc) {
            builder.credential(azkc);
        } else {
            builder.credential((TokenCredential) keyCredential);
        }
        return builder
            .mapsClientId(resolver.resolve(azure.getClientId()))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
    }

    @Override
    public GeoLocationResponse locate(final InetAddress address) {
        val location = new GeoLocationResponse();
        val results = geoLocationClient.getLocation(address);
        if (results.getCountryRegion() != null) {
            location.addAddress(results.getCountryRegion().getIsoCode());
            LOGGER.debug("Geo location for [{}] is calculated as [{}]", address, location);
        }
        return location;
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) {
        val coordinates = new GeoPosition(longitude, latitude);
        val result = mapsSearchClient.getReverseGeocoding(coordinates, List.of(ReverseGeocodingResultTypeEnum.ADDRESS), null);
        val location = new GeoLocationResponse();
        result.getFeatures().forEach(featuresItem ->
            location.addAddress(featuresItem.getProperties().getAddress().getFormattedAddress()));
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}
