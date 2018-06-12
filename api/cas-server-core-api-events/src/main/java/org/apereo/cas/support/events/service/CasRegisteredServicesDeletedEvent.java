package org.apereo.cas.support.events.service;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link CasRegisteredServicesDeletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@ToString
public class CasRegisteredServicesDeletedEvent extends BaseCasRegisteredServiceEvent {
    private static final long serialVersionUID = -8963214046458085393L;

    public CasRegisteredServicesDeletedEvent(final Object source) {
        super(source);
    }
}
