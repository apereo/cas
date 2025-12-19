package org.apereo.cas.configuration.model.core.authentication.risk;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RiskBasedAuthenticationGeoLocationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-electrofence")
public class RiskBasedAuthenticationGeoLocationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4115333388680538358L;

    /**
     * Enable geolocation checking and criteria
     * to calculate risky authentication attempts.
     */
    private boolean enabled;
}
