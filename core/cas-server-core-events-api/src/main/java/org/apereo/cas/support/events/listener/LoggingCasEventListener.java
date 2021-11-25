package org.apereo.cas.support.events.listener;

import org.apereo.cas.support.events.authentication.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.ticket.CasProxyTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketValidatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.util.spring.CasEventListener;

/**
 * Interface for {@code DefaultLoggingCasEventListener} to allow spring {@code @Async} support to use JDK proxy.
 * @author Hal Deadman
 * @since 6.5.0
 */
public interface LoggingCasEventListener extends CasEventListener {
    /**
     * Log {@link CasTicketGrantingTicketCreatedEvent} at debug level.
     *
     * @param e the event
     */
    void logTicketGrantingTicketCreatedEvent(CasTicketGrantingTicketCreatedEvent e);

    /**
     * Log {@link CasAuthenticationTransactionFailureEvent} at debug level.
     *
     * @param e the event
     */
    void logAuthenticationTransactionFailureEvent(CasAuthenticationTransactionFailureEvent e);

    /**
     * Log {@link CasAuthenticationPrincipalResolvedEvent} at debug level.
     *
     * @param e the event
     */
    void logAuthenticationPrincipalResolvedEvent(CasAuthenticationPrincipalResolvedEvent e);

    /**
     * Log {@link CasTicketGrantingTicketDestroyedEvent} at debug level.
     *
     * @param e the event
     */
    void logTicketGrantingTicketDestroyedEvent(CasTicketGrantingTicketDestroyedEvent e);

    /**
     * Log {@link CasProxyTicketGrantedEvent} at debug level.
     *
     * @param e the event
     */
    void logProxyTicketGrantedEvent(CasProxyTicketGrantedEvent e);

    /**
     * Log {@link CasServiceTicketGrantedEvent} at debug level.
     *
     * @param e the event
     */
    void logServiceTicketGrantedEvent(CasServiceTicketGrantedEvent e);

    /**
     * Log {@link CasServiceTicketValidatedEvent} at debug level.
     *
     * @param e the event
     */
    void logServiceTicketValidatedEvent(CasServiceTicketValidatedEvent e);
}
