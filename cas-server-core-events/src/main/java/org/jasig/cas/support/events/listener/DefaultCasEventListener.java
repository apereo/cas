package org.jasig.cas.support.events.listener;

import org.jasig.cas.support.events.CasTicketGrantingTicketCreatedEvent;
import org.jasig.cas.support.events.dao.CasEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * This is {@link DefaultCasEventListener} that attempts to consume CAS events
 * upon various authentication events. Event data is persisted into a repository
 * via {@link CasEventsRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("mongoDbCasEventListener")
public class DefaultCasEventListener {

    @Autowired(required=false)
    @Qualifier("casEventsRepository")
    private CasEventsRepository casEventsRepository;

    /**
     * Handle TGT creation event.
     *
     * @param event the event
     */
    @TransactionalEventListener
    public void handleCasTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent event) {
        if (casEventsRepository != null) {
            casEventsRepository.save(event);
        }
    }
}
