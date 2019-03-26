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
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.J2ESessionStore;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This is {@link DistributedJ2ESessionStore}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DistributedJ2ESessionStore extends J2ESessionStore implements HttpSessionListener, LogoutPostProcessor {
    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;

    @Override
    public Object get(final J2EContext context, final String key) {
        val ticket = getTransientSessionTicketForSession(context);
        if (ticket == null) {
            return null;
        }
        return ticket.getProperties().get(key);
    }

    @Override
    public void set(final J2EContext context, final String key, final Object value) {
        val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val id = getOrCreateSessionId(context);

        val properties = new HashMap<String, Serializable>();
        if (value instanceof Serializable) {
            properties.put(key, (Serializable) value);
        } else if (value != null) {
            LOGGER.trace("Object value [{}] assigned to [{}] is not serializable and will not kept as part of the ticket [{}]", value, key, id);
        }

        var ticket = getTransientSessionTicketForSession(context);
        if (ticket == null) {
            ticket = transientFactory.create(id, properties);
            this.ticketRegistry.addTicket(ticket);
        } else {
            ticket.getProperties().putAll(properties);
            this.ticketRegistry.updateTicket(ticket);
        }
    }

    private TransientSessionTicket getTransientSessionTicketForSession(final J2EContext context) {
        val id = getOrCreateSessionId(context);
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(id);
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
    public void removeSessionTicket(final String id) {
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(id);
        this.ticketRegistry.deleteTicket(ticketId);
    }

    @Override
    public void handle(final TicketGrantingTicket ticketGrantingTicket) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        val id = getOrCreateSessionId(new J2EContext(request, response));
        removeSessionTicket(id);
    }
}
