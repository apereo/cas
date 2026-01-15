package org.apereo.cas.configuration.model.support.geo;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.support.geo.azure.AzureMapsProperties;
import org.apereo.cas.configuration.model.support.geo.googlemaps.GoogleMapsProperties;
import org.apereo.cas.configuration.model.support.geo.ip.IPGeoLocationProperties;
import org.apereo.cas.configuration.model.support.geo.maxmind.MaxmindProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link GeoLocationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-geolocation")
public class GeoLocationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 7529478582792969209L;

    /**
     * IP GeoLocation settings.
     */
    @NestedConfigurationProperty
    private IPGeoLocationProperties ipGeoLocation = new IPGeoLocationProperties();

    /**
     * MaxMind settings.
     */
    @NestedConfigurationProperty
    private MaxmindProperties maxmind = new MaxmindProperties();

    /**
     * Azure Maps GeoLocation settings.
     */
    @NestedConfigurationProperty
    private AzureMapsProperties azure = new AzureMapsProperties();

    /**
     * Google Maps settings.
     */
    @NestedConfigurationProperty
    private GoogleMapsProperties googleMaps = new GoogleMapsProperties();

    /**
     * Groovy settings.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovy = new SpringResourceProperties();
}
