package org.apereo.cas.web.session;

import module java.base;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link TicketRegistryCasSessionStore}. Values are kept as properties of a transient session ticket,
 * and the browser is tied to that ticket with a cookie. Values other than strings are kept as serialized text,
 * so ticket registries that only keep text properties, such as the stateless ticket registry, keep them intact.
 * The cookie carries the id of the ticket as stored by the registry, and is issued again when an update gives
 * the ticket a new id.
 *
 * @author Misagh Moayyed
 * @author Jerome LELEU
 * @since 8.1.0
 * @deprecated Since 7.3.0; Use storage options that store sessions backed by the container, and replicate sessions instead.
 */
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "7.3.0", forRemoval = true)
public class TicketRegistryCasSessionStore implements CasSessionStore {
    private static final String SESSION_ID_IN_REQUEST_ATTRIBUTE = "sessionIdInRequestAttribute";

    private static final String SERIALIZED_VALUE_PREFIX = "rO0AB";

    private final TicketRegistry ticketRegistry;

    private final TicketFactory ticketFactory;

    private final CasCookieBuilder cookieGenerator;

    @Override
    public Optional<String> getSessionId(final HttpServletRequest request, final boolean create) {
        return Optional.ofNullable(fetchSessionIdFromRequest(request));
    }

    @Override
    public Optional<Object> get(final HttpServletRequest request, final String key) {
        LOGGER.trace("Getting key: [{}]", key);
        val ticket = getTransientSessionTicketForSession(request);
        if (ticket == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ticket.getProperties().get(key)).map(TicketRegistryCasSessionStore::decodeValue);
    }

    @Override
    public void set(final HttpServletRequest request, final HttpServletResponse response,
                    final String key, final @Nullable Object value) {
        LOGGER.trace("Setting key: [{}]", key);
        val sessionId = Optional.ofNullable(fetchSessionIdFromRequest(request)).orElseGet(() -> {
            val newSessionId = UUID.randomUUID().toString();
            LOGGER.trace("Generated session id: [{}]", newSessionId);
            return newSessionId;
        });

        val properties = new HashMap<String, Serializable>();
        if (value instanceof final String text) {
            properties.put(key, text);
        } else if (value instanceof final Serializable serializable) {
            properties.put(key, serializeValue(serializable));
        } else if (value != null) {
            LOGGER.warn("Object value [{}] assigned to [{}] is not serializable and may not be part of the ticket [{}]", value, key, sessionId);
        }

        val ticket = getTransientSessionTicketForSession(request);
        if (value == null && ticket != null) {
            ticket.getProperties().remove(key);
            updateTicket(request, response, ticket);
        } else if (ticket == null) {
            FunctionUtils.doAndHandle(_ -> {
                val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
                val transientSessionTicket = transientFactory.create(sessionId, properties);
                val addedTicket = Objects.requireNonNull(ticketRegistry.addTicket(transientSessionTicket));
                cookieGenerator.addCookie(request, response, addedTicket.getId());
                request.setAttribute(SESSION_ID_IN_REQUEST_ATTRIBUTE, addedTicket.getId());
            });
        } else {
            ticket.getProperties().putAll(properties);
            updateTicket(request, response, ticket);
        }
    }

    @Override
    public boolean destroySession(final HttpServletRequest request, final HttpServletResponse response) {
        val sessionId = fetchSessionIdFromRequest(request);
        if (sessionId != null) {
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(sessionId);
            FunctionUtils.doUnchecked(_ -> ticketRegistry.deleteTicket(ticketId));
            cookieGenerator.removeCookie(response);
            LOGGER.trace("Removed session cookie and ticket [{}]", ticketId);
        }
        return true;
    }

    @Override
    public boolean renewSession(final HttpServletRequest request) {
        return false;
    }

    @Override
    public Optional<Object> getTrackableSession(final HttpServletRequest request) {
        val sessionId = fetchSessionIdFromRequest(request);
        LOGGER.trace("Track session id: [{}]", sessionId);
        return Optional.ofNullable(sessionId);
    }

    @Override
    public Optional<CasSessionStore> buildFromTrackableSession(final HttpServletRequest request, final Object trackableSession) {
        request.setAttribute(SESSION_ID_IN_REQUEST_ATTRIBUTE, trackableSession);
        LOGGER.trace("Force session id: [{}]", trackableSession);
        return Optional.of(this);
    }

    protected @Nullable String fetchSessionIdFromRequest(final HttpServletRequest request) {
        var sessionId = (String) request.getAttribute(SESSION_ID_IN_REQUEST_ATTRIBUTE);
        if (StringUtils.isBlank(sessionId)) {
            LOGGER.trace("Session id not found as a request attribute; checking session cookie [{}]", cookieGenerator.getCookieName());
            sessionId = cookieGenerator.retrieveCookieValue(request);
        }
        LOGGER.trace("Fetched session id: [{}]", sessionId);
        return sessionId;
    }

    private void updateTicket(final HttpServletRequest request, final HttpServletResponse response,
                              final TransientSessionTicket ticket) {
        FunctionUtils.doUnchecked(_ -> {
            val updatedTicket = Objects.requireNonNull(ticketRegistry.updateTicket(ticket));
            request.setAttribute(SESSION_ID_IN_REQUEST_ATTRIBUTE, updatedTicket.getId());
            if (!updatedTicket.getId().equals(ticket.getId())) {
                cookieGenerator.addCookie(request, response, updatedTicket.getId());
            }
        });
    }

    private @Nullable TransientSessionTicket getTransientSessionTicketForSession(final HttpServletRequest request) {
        try {
            val sessionId = fetchSessionIdFromRequest(request);
            if (sessionId != null) {
                val ticketId = TransientSessionTicketFactory.normalizeTicketId(sessionId);
                LOGGER.trace("Fetching ticket: [{}]", ticketId);
                return ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            }
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return null;
    }

    private static String serializeValue(final Serializable value) {
        if (value instanceof final Throwable throwable) {
            throwable.setStackTrace(new StackTraceElement[0]);
        }
        return SerializationUtils.serializeBase64(value);
    }

    private static Object decodeValue(final Object value) {
        if (value instanceof final String text && text.startsWith(SERIALIZED_VALUE_PREFIX)) {
            return FunctionUtils.doAndHandle(
                () -> SerializationUtils.deserialize(EncodingUtils.decodeBase64(text), Serializable.class),
                _ -> text).get();
        }
        return value;
    }
}
