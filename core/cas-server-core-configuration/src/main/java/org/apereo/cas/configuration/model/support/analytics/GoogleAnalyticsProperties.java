package org.apereo.cas.configuration.model.support.analytics;

import java.io.Serializable;

/**
 * This is {@link GoogleAnalyticsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAnalyticsProperties implements Serializable {

    private static final long serialVersionUID = 5425678120443123345L;
    /**
     * The tracking id. Configuring the tracking
     * activated google analytics in CAS on UI views, etc.
     */
    private String googleAnalyticsTrackingId;

    public String getGoogleAnalyticsTrackingId() {
        return googleAnalyticsTrackingId;
    }

    public void setGoogleAnalyticsTrackingId(final String googleAnalyticsTrackingId) {
        this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
    }
}
