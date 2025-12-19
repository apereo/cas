package org.apereo.cas.support.events.service;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasRegisteredServiceDeletedEvent}, signaled
 * when a service is removed from the registry.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServiceDeletedEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = -8963214046458085393L;

    private final RegisteredService registeredService;

    public CasRegisteredServiceDeletedEvent(final Object source, final RegisteredService registeredService, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.registeredService = registeredService;
    }
}
