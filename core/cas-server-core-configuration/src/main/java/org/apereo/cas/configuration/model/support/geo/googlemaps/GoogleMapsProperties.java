package org.apereo.cas.configuration.model.support.geo.googlemaps;

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
    private long connectTimeout = 3000;
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
        return connectTimeout;
    }

    public void setConnectTimeout(final long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
