package org.apereo.cas.monitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes the overall health status of the CAS server as determined by composite status values.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class HealthStatus extends Status {

    /** Map of names (e.g. monitor that produced it) to status information. */
    private Map<String, Status> details;

    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     * @param detailMap Map of names to status information. A reasonable name would be, for example, the name of
     *                  the monitor that produced it.
     * @see #getCode()
     */
    public HealthStatus(final StatusCode code, final Map<String, Status> detailMap) {
        super(code);
        this.details = new HashMap<>(detailMap);
    }

    /**
     * Gets the status details comprising the individual health checks performed for overall health status.
     *
     * @return Map of named status items to status information for each check performed.
     */
    public Map<String, Status> getDetails() {
        return this.details;
    }
}
