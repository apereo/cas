package org.apereo.cas.support.events.listener;

import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.util.spring.CasEventListener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Interface for {@code DefaultCasAuthenticationEventListener} to allow spring {@code @Async} support to use JDK proxy.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
public interface CasAuthenticationEventListener extends CasEventListener {

    /**
     * Handle TGT creation event.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @EventListener
    @Async
    void handleCasTicketGrantingTicketCreatedEvent(CasTicketGrantingTicketCreatedEvent event) throws Throwable;

    /**
     * Handle TGT deleted event.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @EventListener
    @Async
    void handleCasTicketGrantingTicketDeletedEvent(CasTicketGrantingTicketDestroyedEvent event) throws Throwable;

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @EventListener
    @Async
    void handleCasAuthenticationTransactionFailureEvent(CasAuthenticationTransactionFailureEvent event) throws Throwable;

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @EventListener
    @Async
    void handleCasAuthenticationPolicyFailureEvent(CasAuthenticationPolicyFailureEvent event) throws Throwable;

    /**
     * Handle cas risky authentication detected event.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @EventListener
    @Async
    void handleCasRiskyAuthenticationDetectedEvent(CasRiskyAuthenticationDetectedEvent event) throws Throwable;

    /**
     * Handle CAS request SLO event.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @EventListener
    @Async
    void handleCasRequestSingleLogoutEvent(CasRequestSingleLogoutEvent event) throws Throwable;
}
