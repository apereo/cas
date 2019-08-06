package org.apereo.cas.integration.pac4j;

import org.apereo.cas.logout.LogoutPostProcessor;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link DistributedJ2ESessionStore}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@RequiredArgsConstructor
@Slf4j
public class DistributedJ2ESessionStore extends JEESessionStore implements HttpSessionListener, LogoutPostProcessor {
    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;

    @Override
    public Optional get(final JEEContext context, final String key) {
        val ticket = getTransientSessionTicketForSession(context);
        if (ticket == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ticket.getProperties().get(key));
    }

    @Override
    public void set(final JEEContext context, final String key, final Object value) {
        val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val id = getOrCreateSessionId(context);

        val properties = new HashMap<String, Serializable>();
        if (value instanceof Serializable) {
            properties.put(key, (Serializable) value);
        } else if (value != null) {
            LOGGER.trace("Object value [{}] assigned to [{}] is not serializable and may not be part of the ticket [{}]",
                value, key, id);
        }

        var ticket = getTransientSessionTicketForSession(context);
        if (value == null && ticket != null) {
            ticket.getProperties().remove(key);
            this.ticketRegistry.updateTicket(ticket);
        } else if (ticket == null) {
            ticket = transientFactory.create(id, properties);
            this.ticketRegistry.addTicket(ticket);
        } else {
            ticket.getProperties().putAll(properties);
            this.ticketRegistry.updateTicket(ticket);
        }
    }

    private TransientSessionTicket getTransientSessionTicketForSession(final JEEContext context) {
        val id = getOrCreateSessionId(context);
        LOGGER.trace("Session identifier is set to [{}]", id);
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(id);

        LOGGER.trace("Fetching session ticket via identifier [{}]", ticketId);
        val ticket = this.ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
        if (ticket == null || ticket.isExpired()) {
            LOGGER.trace("The expiration policy for ticket id [{}] has expired the ticket", ticketId);
            return null;
        }
        return ticket;
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent se) {
        val id = se.getSession().getId();
        removeSessionTicket(id);
    }

    /**
     * Remove session ticket.
     *
     * @param id the id
     */
    private void removeSessionTicket(final String id) {
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(id);
        this.ticketRegistry.deleteTicket(ticketId);
    }

    @Override
    public void handle(final TicketGrantingTicket ticketGrantingTicket) {
        try {
            val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
            val id = getOrCreateSessionId(new JEEContext(request, response, this));
            removeSessionTicket(id);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
    }
}
