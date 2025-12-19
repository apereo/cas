package org.apereo.cas.configuration.model.core.authentication.risk;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RiskBasedAuthenticationCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-electrofence")
public class RiskBasedAuthenticationCoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 511801361041617794L;

    /**
     * The risk threshold factor beyond which the authentication
     * event may be considered risky.
     */
    @RequiredProperty
    private double threshold = 0.6;

    /**
     * Indicates how far back the search in authentication history must go
     * in order to locate authentication events.
     */
    private long daysInRecentHistory = 30;
}
