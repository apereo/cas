package org.apereo.cas.support.events.listener;

import org.apereo.cas.support.events.authentication.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.ticket.CasProxyTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketValidatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static org.apereo.cas.util.serialization.MessageSanitizationUtils.*;

/**
 * Listener implementation for core CAS events which adds contextual
 * debug logging for each event to instrument CAS logs
 * with more information on internal CAS processing to aid troubleshooting, etc.
 *
 * @author Dmitriy Kopylenko
 * @since 6.1.0
 */
@Slf4j
public class DefaultLoggingCasEventListener implements LoggingCasEventListener {

    private static final String GRANTED_TGT_MSG = "Established SSO session at [{}]\nTGT: [{}], With TimeToLive: [{}], TimeToIdle: [{}]\n"
                                                  + "For principal: [{}]";

    private static final String AUTHN_TX_FAIL_MSG = "Authentication transaction failed for credential: [{}]\nFailure(s): [{}]";

    private static final String PRINCIPAL_RESOLVED_MSG = "Principal [{}] resolved\nWith attributes: [{}]";

    private static final String DESTROYED_TGT_MSG = "SSO session ended\nTGT creation time: [{}] TGT: [{}], With TimeToLive: [{}], TimeToIdle: [{}]\n"
                                                    + "For principal: [{}]\nInitiator source: [{}]";

    private static final String GRANTED_PT_MSG = "Proxy ticket granted at: [{}]\nPT: [{}], With TimeToLive: [{}], TimeToIdle: [{}]\n"
                                                 + "By PGT: [{}], For service: [{}]\nProxied by: [{}]\nFor principal: [{}]";

    private static final String CREATED_ST_MSG = "Service ticket created at [{}]\nST: [{}]\nFor service: [{}]\nFor principal: [{}]";

    private static final String VALIDATED_ST_MSG = "Service ticket validated at [{}]\nST: [{}]\nFor service: [{}]\n"
                                                   + "For principal: [{}]\nWith released attributes: [{}]";

    @Override
    public void logTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent e) {
        val tgtId = sanitize(e.getId());
        LOGGER.debug(GRANTED_TGT_MSG,
            e.getCreationTime(),
            tgtId,
            e.getTimeToLive(),
            e.getTimeToIdle(),
            e.getPrincipalId());
    }

    @Override
    public void logAuthenticationTransactionFailureEvent(final CasAuthenticationTransactionFailureEvent e) {
        LOGGER.debug(AUTHN_TX_FAIL_MSG, e.getCredential(), e.getFailures());
    }

    @Override
    public void logAuthenticationPrincipalResolvedEvent(final CasAuthenticationPrincipalResolvedEvent e) {
        LOGGER.debug(PRINCIPAL_RESOLVED_MSG, e.getPrincipal().getId(), e.getPrincipal().getAttributes());
    }

    @Override
    public void logTicketGrantingTicketDestroyedEvent(final CasTicketGrantingTicketDestroyedEvent e) {
        val tgtId = sanitize(e.getId());
        LOGGER.debug(DESTROYED_TGT_MSG,
            e.getCreationTime(),
            tgtId,
            e.getTimeToLive(),
            e.getTimeToIdle(),
            e.getPrincipalId(),
            e.getSource().getClass().getName());
    }

    @Override
    public void logProxyTicketGrantedEvent(final CasProxyTicketGrantedEvent e) {
        val pgt = e.getProxyGrantingTicket();
        val pt = e.getProxyTicket();
        val pgtId = sanitize(pgt.getId());
        val ptId = sanitize(pt.getId());
        val principal = pgt.getAuthentication().getPrincipal().getId();
        LOGGER.debug(GRANTED_PT_MSG,
            pt.getCreationTime(),
            ptId,
            pt.getExpirationPolicy().getTimeToLive(),
            pt.getExpirationPolicy().getTimeToIdle(),
            pgtId,
            pt.getService().getId(),
            pgt.getProxiedBy().getId(),
            principal);
    }

    @Override
    public void logServiceTicketGrantedEvent(final CasServiceTicketGrantedEvent e) {
        val serviceTicket = e.getServiceTicket();
        LOGGER.debug(CREATED_ST_MSG,
            serviceTicket.getCreationTime(),
            serviceTicket.getId(),
            serviceTicket.getService().getId(),
            e.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
    }

    @Override
    public void logServiceTicketValidatedEvent(final CasServiceTicketValidatedEvent e) {
        val serviceTicket = e.getServiceTicket();
        val principal = serviceTicket.getTicketGrantingTicket().getAuthentication().getPrincipal();
        LOGGER.debug(VALIDATED_ST_MSG,
            serviceTicket.getCreationTime(),
            serviceTicket.getId(),
            serviceTicket.getService().getId(),
            principal.getId(),
            principal.getAttributes());
    }
}
