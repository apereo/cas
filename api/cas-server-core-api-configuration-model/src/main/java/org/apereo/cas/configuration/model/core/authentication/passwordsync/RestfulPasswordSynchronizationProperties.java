package org.apereo.cas.configuration.model.core.authentication.passwordsync;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link RestfulPasswordSynchronizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-pm")
@Getter
@Setter
@Accessors(chain = true)
public class RestfulPasswordSynchronizationProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = 5656875085138384223L;

    /**
     * Indicate whether provisioning should be asynchronous.
     */
    private boolean asynchronous;
}
