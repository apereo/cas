package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration properties class for cas.throttle.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-throttle")
@Getter
@Setter
@Accessors(chain = true)

public class ThrottleFailureProperties implements Serializable {
    /**
     * Default authentication failed action used as the code.
     */
    private static final String DEFAULT_AUTHN_FAILED_ACTION = "AUTHENTICATION_FAILED";

    @Serial
    private static final long serialVersionUID = 7647772524660134142L;

    /**
     * Failure code to record in the audit log.
     * Generally this indicates an authentication failure event.
     */
    private String code = DEFAULT_AUTHN_FAILED_ACTION;

    /**
     * Number of failed login attempts for the threshold rate.
     */
    private int threshold = -1;

    /**
     * Period of time in seconds for the threshold rate.
     */
    private int rangeSeconds = -1;

    /**
     * Indicate the number of seconds the account should remain
     * in a locked/throttled state before it can be released
     * to continue again. If no value is specified, the failure
     * threshold and rate that is calculated would hold.
     */
    @DurationCapable
    private String throttleWindowSeconds = "0";
}
