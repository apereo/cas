package org.apereo.cas.configuration.model.support.analytics;

/**
 * This is {@link GoogleAnalyticsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class GoogleAnalyticsProperties {

    private String googleAnalyticsTrackingId;

    public String getGoogleAnalyticsTrackingId() {
        return googleAnalyticsTrackingId;
    }

    public void setGoogleAnalyticsTrackingId(final String googleAnalyticsTrackingId) {
        this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
    }
}
