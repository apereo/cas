package org.apereo.cas.support.events.service;

import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.ToString;

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
}
