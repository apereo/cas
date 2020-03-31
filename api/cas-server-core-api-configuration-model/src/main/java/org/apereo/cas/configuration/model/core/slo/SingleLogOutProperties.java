package org.apereo.cas.configuration.model.core.slo;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link SingleLogOutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class SingleLogOutProperties implements Serializable {

    private static final long serialVersionUID = 3676710533477055700L;

    /**
     * Whether SLO callbacks should be done in an asynchronous manner via the HTTP client.
     * When true, CAS will not wait for the operation to fully complete and will resume control to carry on.
     */
    private boolean asynchronous = true;

    /**
     * Whether SLO should be entirely disabled globally for the CAS deployment.
     */
    private boolean disabled;
}
