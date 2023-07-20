package org.apereo.cas.support.events.service;

import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * This is {@link CasRegisteredServiceLoadedEvent} that is signaled
 * when a registered service is loaded from the registry.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServiceLoadedEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = 290968299766263298L;

    private final RegisteredService registeredService;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source            the source
     * @param registeredService the registered service
     */
    public CasRegisteredServiceLoadedEvent(final Object source, final RegisteredService registeredService, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.registeredService = registeredService;
    }
}
