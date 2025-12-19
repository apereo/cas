package org.apereo.cas.support.events.service;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasRegisteredServicesLoadedEvent} that is signaled
 * when registered service are loaded into the CAS registry.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServicesLoadedEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = 291168299712263298L;

    private final Collection<RegisteredService> services;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source   the source
     * @param services collection of loaded services
     */
    public CasRegisteredServicesLoadedEvent(final Object source, final Collection<RegisteredService> services, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.services = services;
    }
}
