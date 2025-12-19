package org.apereo.cas.support.events.service;

import module java.base;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasRegisteredServicesDeletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServicesDeletedEvent extends BaseCasRegisteredServiceEvent {
    @Serial
    private static final long serialVersionUID = -8963214046458085393L;

    public CasRegisteredServicesDeletedEvent(final Object source, final ClientInfo clientInfo) {
        super(source, clientInfo);
    }
}
