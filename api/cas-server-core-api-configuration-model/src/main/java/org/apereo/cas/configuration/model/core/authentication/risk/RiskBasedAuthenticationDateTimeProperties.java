package org.apereo.cas.configuration.model.core.authentication.risk;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RiskBasedAuthenticationDateTimeProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-electrofence")
public class RiskBasedAuthenticationDateTimeProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -3776875583039922050L;

    /**
     * Enable date/time checking and criteria
     * to calculate risky authentication attempts.
     */
    private boolean enabled;

    /**
     * The hourly window used before and after each authentication event
     * in calculation to establish a pattern that can then be compared against the threshold.
     */
    private int windowInHours = 2;
}
