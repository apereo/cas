package org.apereo.cas.configuration.model.support.geo.googlemaps;

import org.apereo.cas.configuration.support.Beans;

/**
 * This is {@link GoogleMapsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleMapsProperties {
    private String apiKey;
    private String clientId;
    private String clientSecret;
    private String connectTimeout = "PT3S";
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
