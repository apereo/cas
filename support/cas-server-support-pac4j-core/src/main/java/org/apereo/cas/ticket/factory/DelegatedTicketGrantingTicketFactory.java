package org.apereo.cas.ticket.factory;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * The ticket-granting-ticket factory for the authn delegation.
 * The delegated session key is added to the TGT.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
public class DelegatedTicketGrantingTicketFactory extends DefaultTicketGrantingTicketFactory {

    /**
     * The request attribute used to identity the delegated session key.
     */
    public static final String DELEGATED_SESSION_KEY_REQUEST_ATTRIBUTE = "delegatedSessionKey";

    public DelegatedTicketGrantingTicketFactory(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                                                final ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicy,
                                                final CipherExecutor<Serializable, String> cipherExecutor,
                                                final ServicesManager servicesManager) {
        super(ticketGrantingTicketUniqueTicketIdGenerator, ticketGrantingTicketExpirationPolicy, cipherExecutor, servicesManager);
    }

    @Override
    protected void supplementTicket(final TicketGrantingTicketImpl ticket) {
        val requestContext = RequestContextHolder.getRequestContext();
        if (requestContext != null) {
            val externalContext = requestContext.getExternalContext();
            if (externalContext instanceof ServletExternalContext sec) {
                val request = (HttpServletRequest) sec.getNativeRequest();
                val key = (String) request.getAttribute(DELEGATED_SESSION_KEY_REQUEST_ATTRIBUTE);
                if (StringUtils.isNotBlank(key)) {
                    ticket.setLinkedIdentifier(key);
                }
            }
        }
    }
}
