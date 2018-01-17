package org.apereo.cas.support.events.service;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.RegisteredService;
import lombok.ToString;

/**
 * This is {@link CasRegisteredServiceSavedEvent} that is signaled
 * when a registered service is saved into the CAS registry.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@ToString
public class CasRegisteredServiceSavedEvent extends BaseCasRegisteredServiceEvent {

    private static final long serialVersionUID = 291168299766263298L;

    private final RegisteredService registeredService;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source            the source
     * @param registeredService the registered service
     */
    public CasRegisteredServiceSavedEvent(final Object source, final RegisteredService registeredService) {
        super(source);
        this.registeredService = registeredService;
    }

    public RegisteredService getRegisteredService() {
        return this.registeredService;
    }
}
