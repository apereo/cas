package org.apereo.cas.configuration.model.support.analytics;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link GoogleAnalyticsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-google-analytics", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class GoogleAnalyticsProperties implements Serializable {

    private static final long serialVersionUID = 5425678120443123345L;

    /**
     * The tracking id. Configuring the tracking
     * activated google analytics in CAS on UI views, etc.
     */
    private String googleAnalyticsTrackingId;

    /**
     * Cookie settings to be used with google analytics.
     */
    private Cookie cookie = new Cookie();

    @RequiresModule(name = "cas-server-support-google-analytics", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Cookie extends CookieProperties {
        private static final long serialVersionUID = -5432498833437602657L;

        /**
         * Attribute name to collect
         * from the authentication event
         * to serve as the cookie value.
         */
        private String attributeName;

        /**
         * A regular expression pattern
         * that is tested against attribute values
         * to only release and allow those that produce
         * a successful match.
         */
        private String attributeValuePattern = ".+";

        public Cookie() {
            setName("CasGoogleAnalytics");
            setPinToSession(false);
        }
    }
}
