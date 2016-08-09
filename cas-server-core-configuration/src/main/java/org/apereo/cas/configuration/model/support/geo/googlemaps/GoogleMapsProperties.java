package org.apereo.cas.configuration.model.support.geo.googlemaps;

/**
 * This is {@link GoogleMapsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleMapsProperties {
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }
}
