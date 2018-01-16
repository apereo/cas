package org.apereo.cas.support.events.service;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.RegisteredService;
import lombok.ToString;

/**
 * This is {@link CasRegisteredServiceDeletedEvent}, signaled
 * when a service is removed from the registry.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString
public class CasRegisteredServiceDeletedEvent extends BaseCasRegisteredServiceEvent {

    private static final long serialVersionUID = -8963214046458085393L;

    private final RegisteredService registeredService;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source            the source
     * @param registeredService the registered service
     */
    public CasRegisteredServiceDeletedEvent(final Object source, final RegisteredService registeredService) {
        super(source);
        this.registeredService = registeredService;
    }

    public RegisteredService getRegisteredService() {
        return this.registeredService;
    }
}
