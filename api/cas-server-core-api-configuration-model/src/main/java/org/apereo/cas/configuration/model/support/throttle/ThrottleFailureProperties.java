package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("ThrottleFailureProperties")
public class ThrottleFailureProperties implements Serializable {
    /**
     * Default authentication failed action used as the code.
     */
    private static final String DEFAULT_AUTHN_FAILED_ACTION = "AUTHENTICATION_FAILED";

    /**
     * Failure code to record in the audit log.
     * Generally this indicates an authentication failure event.
     */
    private String code = DEFAULT_AUTHN_FAILED_ACTION;

    /**
     * Number of failed login attempts permitted in the above period.
     * All login throttling components that ship with CAS limit successive failed
     * login attempts that exceed a threshold rate in failures per second.
     */
    private int threshold = -1;

    /**
     * Period of time in seconds during which the threshold applies.
     */
    private int rangeSeconds = -1;

}
