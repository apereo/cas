package org.apereo.cas.support.events.service;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link CasRegisteredServicePreDeleteEvent}, signaled
 * when a service about to be removed from the registry.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
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

    public RegisteredService getRegisteredService() {
        return this.registeredService;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("registeredService", this.registeredService)
                .toString();
    }
}
