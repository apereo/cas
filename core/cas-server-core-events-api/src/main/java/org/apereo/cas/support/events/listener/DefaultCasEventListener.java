package org.apereo.cas.support.events.listener;

import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.util.spring.CasEventListener;

/**
 * Interface for {@code DefaultCasEventListenerImpl} to allow spring {@code @Async} support to use JDK proxy.
 * @author Hal Deadman
 * @since 6.5.0
 */
public interface DefaultCasEventListener extends CasEventListener {

    /**
     * Handle TGT creation event.
     *
     * @param event the event
     */
    void handleCasTicketGrantingTicketCreatedEvent(CasTicketGrantingTicketCreatedEvent event);

    /**
     * Handle TGT deleted event.
     *
     * @param event the event
     */
    void handleCasTicketGrantingTicketDeletedEvent(CasTicketGrantingTicketDestroyedEvent event);

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     */
    void handleCasAuthenticationTransactionFailureEvent(CasAuthenticationTransactionFailureEvent event);

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     */
    void handleCasAuthenticationPolicyFailureEvent(CasAuthenticationPolicyFailureEvent event);

    /**
     * Handle cas risky authentication detected event.
     *
     * @param event the event
     */
    void handleCasRiskyAuthenticationDetectedEvent(CasRiskyAuthenticationDetectedEvent event);
}
