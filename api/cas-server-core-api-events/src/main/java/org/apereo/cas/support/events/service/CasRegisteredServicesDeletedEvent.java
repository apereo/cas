package org.apereo.cas.support.events.service;

import lombok.Getter;
import lombok.ToString;

/**
 * This is {@link CasRegisteredServicesDeletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServicesDeletedEvent extends BaseCasRegisteredServiceEvent {
    private static final long serialVersionUID = -8963214046458085393L;

    public CasRegisteredServicesDeletedEvent(final Object source) {
        super(source);
    }
}
