package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("CoreEventsProperties")
public class CoreEventsProperties implements Serializable {

    private static final long serialVersionUID = 2734523424737956370L;

    /**
     * Whether event tracking and recording functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Whether geolocation should be tracked as part of collected authentication events.
     * This of course requires consent from the user's browser to collect stats on location.
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
