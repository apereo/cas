package org.apereo.cas.configuration.model.support.analytics;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GoogleAnalyticsCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-google-analytics")
@Getter
@Setter
@Accessors(chain = true)
public class GoogleAnalyticsCookieProperties extends CookieProperties {
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

    public GoogleAnalyticsCookieProperties() {
        setName("CasGoogleAnalytics");
    }
}
