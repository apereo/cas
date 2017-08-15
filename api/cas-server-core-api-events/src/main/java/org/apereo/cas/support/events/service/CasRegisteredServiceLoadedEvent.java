package org.apereo.cas.support.events.service;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link CasRegisteredServiceLoadedEvent} that is signaled
 * when a registered service is loaded from the registry.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasRegisteredServiceLoadedEvent extends BaseCasRegisteredServiceEvent {

    private static final long serialVersionUID = 290988299766263298L;
    private final RegisteredService registeredService;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source            the source
     * @param registeredService the registered service
     */
    public CasRegisteredServiceLoadedEvent(final Object source, final RegisteredService registeredService) {
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
