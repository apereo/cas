package org.apereo.cas.configuration.model.core.authentication.risk;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link RiskBasedAuthenticationDeviceFingerprintProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-electrofence")
public class RiskBasedAuthenticationDeviceFingerprintProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -3776875583039922051L;

    /**
     * Enable date/time checking and criteria
     * to calculate risky authentication attempts.
     */
    private boolean enabled;
}
