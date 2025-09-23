package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration properties class for events.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-events", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class CoreEventsProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2734523424737956370L;

    /**
     * Whether event tracking and recording functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Whether geolocation should be tracked as part of collected authentication events.
     * This of course requires consent from the user's browser to collect stats on location.
     * Turning on this setting would prompt the browser to ask for user's consent to collect
     * geo location directly. If geo location information is not available using this strategy,
     * it may still be extracted and determined based on the client IP address at the time
     * the event is being recorded and captured by CAS.
     */
    private boolean trackGeolocation;

    /**
     * Whether CAS should track the underlying configuration store for changes.
     * This depends on whether the store provides that sort of functionality.
     * When running in standalone mode, this typically translates to CAS monitoring
     * configuration files and reloading context conditionally if there are any changes.
     */
    private boolean trackConfigurationModifications;
}
