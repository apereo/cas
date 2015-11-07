package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.services.RegisteredService;

/**
 * This is {@link CasRegisteredServiceDeletedEvent}, signaled
 * when a service is removed from the registry.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class CasRegisteredServiceDeletedEvent extends AbstractCasEvent {

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
        return registeredService;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("registeredService", registeredService)
                .toString();
    }
}
