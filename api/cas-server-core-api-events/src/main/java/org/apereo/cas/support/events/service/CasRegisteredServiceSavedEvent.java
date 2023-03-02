package org.apereo.cas.support.events.service;

import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * This is {@link CasRegisteredServiceSavedEvent} that is signaled
 * when a registered service is saved into the CAS registry.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServiceSavedEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = 291168299766263298L;

    private final RegisteredService registeredService;

    public CasRegisteredServiceSavedEvent(final Object source, final RegisteredService registeredService, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.registeredService = registeredService;
    }
}
