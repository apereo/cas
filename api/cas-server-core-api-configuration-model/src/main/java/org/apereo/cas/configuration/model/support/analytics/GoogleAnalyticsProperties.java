package org.apereo.cas.configuration.model.support.analytics;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link GoogleAnalyticsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-google-analytics")
@Getter
@Setter
@Accessors(chain = true)

public class GoogleAnalyticsProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 5425678120443123345L;

    /**
     * The tracking id. Configuring the tracking
     * activated google analytics in CAS on UI views, etc.
     */
    @RequiredProperty
    private String googleAnalyticsTrackingId;

    /**
     * Cookie settings to be used with google analytics.
     */
    @NestedConfigurationProperty
    private GoogleAnalyticsCookieProperties cookie = new GoogleAnalyticsCookieProperties();

}
