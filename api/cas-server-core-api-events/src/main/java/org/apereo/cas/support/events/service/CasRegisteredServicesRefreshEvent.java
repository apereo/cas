package org.apereo.cas.support.events.service;

/**
 * This is {@link CasRegisteredServicesRefreshEvent} that is signaled
 * when a registered service is saved into the CAS registry.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class CasRegisteredServicesRefreshEvent extends BaseCasRegisteredServiceEvent {

    private static final long serialVersionUID = 291168299766263298L;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source the source
     */
    public CasRegisteredServicesRefreshEvent(final Object source) {
        super(source);
    }
    
}
