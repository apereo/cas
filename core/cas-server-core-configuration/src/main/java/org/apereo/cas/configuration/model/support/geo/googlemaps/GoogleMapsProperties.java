package org.apereo.cas.configuration.model.support.geo.googlemaps;

import org.apereo.cas.configuration.support.Beans;

import java.io.Serializable;

/**
 * This is {@link GoogleMapsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleMapsProperties implements Serializable {
    private static final long serialVersionUID = 4661113818711911462L;
    /**
     * Authenticate into google maps via an API key.
     */
    private String apiKey;
    /**
     * Authenticate into google maps via a client id.
     */
    private String clientId;
    /**
     * Authenticate into google maps via a client secret.
     */
    private String clientSecret;
    /**
     * The connection timeout when reaching out to google maps.
     */
    private String connectTimeout = "PT3S";
    /**
     * When true, a strategy for handling URL requests using Google App Engine's URL Fetch API.
     */
    private boolean googleAppsEngine;

    public boolean isGoogleAppsEngine() {
        return googleAppsEngine;
    }

    public void setGoogleAppsEngine(final boolean googleAppsEngine) {
        this.googleAppsEngine = googleAppsEngine;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public long getConnectTimeout() {
        return Beans.newDuration(connectTimeout).toMillis();
    }

    public void setConnectTimeout(final String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
