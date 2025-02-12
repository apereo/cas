package org.apereo.cas.support.events.service;

import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * This is {@link CasRegisteredServiceExpiredEvent} that is signaled
 * when a registered service is expired.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServiceExpiredEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = 291168299766263298L;

    private final RegisteredService registeredService;

    private final boolean deleted;

    public CasRegisteredServiceExpiredEvent(final Object source, final RegisteredService registeredService,
                                            final boolean deleted, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.registeredService = registeredService;
        this.deleted = deleted;
    }
}
