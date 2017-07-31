package org.apereo.cas.support.events.service;

import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link BaseCasRegisteredServiceEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseCasRegisteredServiceEvent extends AbstractCasEvent {
    private static final long serialVersionUID = 7828374109804253319L;

    public BaseCasRegisteredServiceEvent(final Object source) {
        super(source);
    }
}
