package org.apereo.cas.configuration.model.support.analytics;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link GoogleAnalyticsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
public class GoogleAnalyticsProperties implements Serializable {

    private static final long serialVersionUID = 5425678120443123345L;

    /**
     * The tracking id. Configuring the tracking
     * activated google analytics in CAS on UI views, etc.
     */
    private String googleAnalyticsTrackingId;
}
