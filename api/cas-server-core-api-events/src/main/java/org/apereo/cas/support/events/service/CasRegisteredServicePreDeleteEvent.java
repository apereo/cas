package org.apereo.cas.support.events.service;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasRegisteredServicePreDeleteEvent}, signaled
 * when a service about to be removed from the registry.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServicePreDeleteEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = -8964760046458085393L;

    private final RegisteredService registeredService;

    public CasRegisteredServicePreDeleteEvent(final Object source, final RegisteredService registeredService, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.registeredService = registeredService;
    }
}
