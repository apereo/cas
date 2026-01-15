package org.apereo.cas.support.events.service;

import module java.base;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.inspektr.common.web.ClientInfo;


/**
 * This is {@link BaseCasRegisteredServiceEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseCasRegisteredServiceEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = 7828374109804253319L;

    protected BaseCasRegisteredServiceEvent(final Object source, final ClientInfo clientInfo) {
        super(source, clientInfo);
    }
}
