package org.apereo.cas.session;

import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This is {@link TicketRegistrySessionRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class TicketRegistrySessionRepository extends MapSessionRepository {
    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;

    public TicketRegistrySessionRepository(final TicketRegistry ticketRegistry,
                                           final TicketFactory ticketFactory) {
        super(new ConcurrentHashMap<>());
        this.ticketRegistry = ticketRegistry;
        this.ticketFactory = ticketFactory;
    }

    @Override
    public void save(final MapSession session) {
        FunctionUtils.doUnchecked(__ -> {
            if (!session.getId().equals(session.getOriginalId())) {
                deleteById(session.getOriginalId());
            }
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(session.getId());
            try {
                val currentTicket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
                currentTicket.getProperties().putAll(convertSessionAttributes(session));
                LOGGER.trace("Updating session [{}] with properties [{}]", currentTicket.getId(), currentTicket);
                ticketRegistry.updateTicket(currentTicket);
            } catch (final InvalidTicketException e) {
                val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
                val properties = convertSessionAttributes(session);
                val ticket = factory.create(ticketId, properties);
                LOGGER.trace("Saving session [{}] with properties [{}]", ticket.getId(), ticket);
                ticketRegistry.addTicket(ticket);
            }
        });
    }

    @Override
    public MapSession findById(final String id) {
        try {
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(id);
            LOGGER.trace("Finding session by id [{}]", ticketId);
            val ticket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            val newSession = new MapSession(ticket.getId());
            newSession.setCreationTime(ticket.getProperty("creationTime", Instant.class));
            newSession.setLastAccessedTime(ticket.getProperty("lastAccessedTime", Instant.class));
            val sessionAttributes = ticket.getProperty("attributes", Map.class);
            sessionAttributes.forEach((key, value) -> {
                val decoded = EncodingUtils.decodeBase64(value.toString());
                val attributeValue = SerializationUtils.deserialize(decoded, Serializable.class);
                newSession.setAttribute(key.toString(), attributeValue);
            });
            LOGGER.trace("Found session [{}] with attributes [{}]", newSession.getId(), sessionAttributes);
            return newSession;
        } catch (final InvalidTicketException e) {
            LOGGER.trace("Session with id [{}] not found", id, e);
            return null;
        }
    }

    @Override
    public void deleteById(final String id) {
        FunctionUtils.doUnchecked(__ -> {
            LOGGER.trace("Deleting session by id [{}]", id);
            ticketRegistry.deleteTicket(id);
        });
    }

    private static Map<String, Object> convertSessionAttributes(final MapSession session) {
        val properties = new LinkedHashMap<String, Object>();
        properties.put("lastAccessedTime", session.getLastAccessedTime());
        properties.put("creationTime", session.getCreationTime());
        properties.put("originalId", session.getOriginalId());
        properties.put("id", session.getId());
        val sessionAttributes = session.getAttributeNames()
            .stream()
            .map(name -> {
                val value = (Serializable) session.getAttribute(name);
                return value == null ? null : Pair.of(name, SerializationUtils.serializeBase64(value));
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        properties.put("attributes", sessionAttributes);
        return properties;
    }
}
