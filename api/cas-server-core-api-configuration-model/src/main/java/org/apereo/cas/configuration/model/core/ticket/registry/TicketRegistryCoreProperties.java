package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link TicketRegistryCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class TicketRegistryCoreProperties implements Serializable {
    private static final long serialVersionUID = -6927362599655259000L;

    /**
     * When set to true, registry operations will begin to support
     * distributed locking for ticket operations. If the registry
     * itself supports distributed locking, such as JDBC or Redis,
     * then the lock implementation will defer to that option. Otherwise
     * the default locking solution will be specific to a CAS server node,
     * until replaced with a lock implementation or different locking option
     * separate from the registry technology itself.
     */
    private boolean enableLocking = true;
}
