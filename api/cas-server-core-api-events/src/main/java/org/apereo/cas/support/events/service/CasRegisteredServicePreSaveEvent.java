package org.apereo.cas.support.events.service;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasRegisteredServicePreSaveEvent} that is signaled
 * when a registered service about to be saved into the registry.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServicePreSaveEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = 291988299766263298L;

    private final RegisteredService registeredService;

    public CasRegisteredServicePreSaveEvent(final Object source, final RegisteredService registeredService, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.registeredService = registeredService;
    }
}
