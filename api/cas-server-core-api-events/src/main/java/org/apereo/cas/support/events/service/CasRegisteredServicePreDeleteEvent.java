package org.apereo.cas.support.events.service;

import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.ToString;

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

    private static final long serialVersionUID = -8964760046458085393L;

    private final RegisteredService registeredService;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source            the source
     * @param registeredService the registered service
     */
    public CasRegisteredServicePreDeleteEvent(final Object source, final RegisteredService registeredService) {
        super(source);
        this.registeredService = registeredService;
    }
}
