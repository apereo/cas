package org.apereo.cas.integration.pac4j;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutPostProcessor;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link DistributedJ2ESessionStore}.
 *
 * @author Misagh Moayyed
 * @author Jerome LELEU
 * @since 6.1.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
public class DistributedJ2ESessionStore implements SessionStore<JEEContext>, LogoutPostProcessor {
    private static final String SESSION_ID_IN_REQUEST_ATTRIBUTE = "sessionIdInRequestAttribute";
    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;
    private final CookieRetrievingCookieGenerator cookieGenerator;

    public DistributedJ2ESessionStore(final TicketRegistry ticketRegistry, final TicketFactory ticketFactory,
                                      final CasConfigurationProperties casProperties) {
        this.ticketRegistry = ticketRegistry;
        this.ticketFactory = ticketFactory;
        val tgc = casProperties.getTgc();
        val context = CookieGenerationContext.builder()
                .name(casProperties.getSessionReplication().getSessionCookieName())
                .path(tgc.getPath()).maxAge(tgc.getMaxAge()).secure(tgc.isSecure())
                .domain(tgc.getDomain()).httpOnly(tgc.isHttpOnly()).build();
        this.cookieGenerator = new CookieRetrievingCookieGenerator(context);
    }

    @Override
    public String getOrCreateSessionId(final JEEContext context) {
        var sessionId = (String) context.getRequestAttribute(SESSION_ID_IN_REQUEST_ATTRIBUTE).orElse(null);
        if (sessionId == null) {
            sessionId = cookieGenerator.retrieveCookieValue(context.getNativeRequest());
        }
        LOGGER.trace("retrieved sessionId: {}", sessionId);
        if (sessionId == null) {
            sessionId = java.util.UUID.randomUUID().toString();
            LOGGER.debug("generated sessionId: {}", sessionId);
            cookieGenerator.addCookie(context.getNativeRequest(), context.getNativeResponse(), sessionId);
            context.setRequestAttribute(SESSION_ID_IN_REQUEST_ATTRIBUTE, sessionId);
        }
        return sessionId;
    }

    @Override
    public Optional get(final JEEContext context, final String key) {
        LOGGER.trace("getting key: {}", key);
        val ticket = getTransientSessionTicketForSession(context);
        if (ticket == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ticket.getProperties().get(key));
    }

    @Override
    public void set(final JEEContext context, final String key, final Object value) {
        LOGGER.trace("setting key: {}", key);
        val sessionId = getOrCreateSessionId(context);

        val properties = new HashMap<String, Serializable>();
        if (value instanceof Serializable) {
            properties.put(key, (Serializable) value);
        } else if (value != null) {
            LOGGER.trace("Object value [{}] assigned to [{}] is not serializable and may not be part of the ticket [{}]",
                    value, key, sessionId);
        }

        var ticket = getTransientSessionTicketForSession(context);
        if (value == null && ticket != null) {
            ticket.getProperties().remove(key);
            this.ticketRegistry.updateTicket(ticket);
            LOGGER.trace("updated ticket: {}", ticket.getId());
        } else if (ticket == null) {
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            ticket = transientFactory.create(sessionId, properties);
            this.ticketRegistry.addTicket(ticket);
            LOGGER.trace("added ticket: {}", ticket.getId());
        } else {
            ticket.getProperties().putAll(properties);
            this.ticketRegistry.updateTicket(ticket);
            LOGGER.trace("updated ticket: {}", ticket.getId());
        }
    }

    private TransientSessionTicket getTransientSessionTicketForSession(final JEEContext context) {
        val sessionId = getOrCreateSessionId(context);
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(sessionId);

        LOGGER.trace("fetching ticket: {}", ticketId);
        val ticket = this.ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
        if (ticket == null || ticket.isExpired()) {
            LOGGER.trace("ticket {} does not exist or is expired", ticketId);
            return null;
        }
        return ticket;
    }

    @Override
    public void handle(final TicketGrantingTicket ticketGrantingTicket) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        if (request != null && response != null) {
            destroySession(new JEEContext(request, response, this));
        }
    }

    @Override
    public boolean destroySession(final JEEContext context) {
        try {
            val sessionId = getOrCreateSessionId(context);
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(sessionId);
            this.ticketRegistry.deleteTicket(ticketId);
            cookieGenerator.removeCookie(context.getNativeResponse());
            LOGGER.trace("remove session cookie and ticket: {}", ticketId);
            return true;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Optional getTrackableSession(final JEEContext context) {
        val sessionId = getOrCreateSessionId(context);
        LOGGER.trace("track sessionId: {}", sessionId);
        return Optional.of(sessionId);
    }

    @Override
    public Optional<SessionStore<JEEContext>> buildFromTrackableSession(final JEEContext context, final Object trackableSession) {
        context.setRequestAttribute(SESSION_ID_IN_REQUEST_ATTRIBUTE, trackableSession);
        LOGGER.trace("force sessionId: {}", trackableSession);
        return Optional.of(this);
    }

    @Override
    public boolean renewSession(final JEEContext context) {
        return false;
    }
}
