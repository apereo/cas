package org.apereo.cas.configuration.model.support.geo.googlemaps;

import org.apereo.cas.configuration.model.support.geo.BaseGeoLocationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GoogleMapsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-geolocation-googlemaps")
@Getter
@Accessors(chain = true)
@Setter
public class GoogleMapsProperties extends BaseGeoLocationProperties {

    private static final long serialVersionUID = 4661113818711911462L;

    /**
     * Authenticate into google maps via an API key.
     */
    @RequiredProperty
    private String apiKey;

    /**
     * Authenticate into google maps via a client id.
     */
    @RequiredProperty
    private String clientId;

    /**
     * Authenticate into google maps via a client secret.
     */
    @RequiredProperty
    private String clientSecret;

    /**
     * The connection timeout when reaching out to google maps.
     */
    private String connectTimeout = "PT3S";

    /**
     * When true, a strategy for handling URL requests using Google App Engine's URL Fetch API.
     */
    private boolean googleAppsEngine;
}
