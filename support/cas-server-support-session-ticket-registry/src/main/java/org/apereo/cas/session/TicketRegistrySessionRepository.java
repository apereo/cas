package org.apereo.cas.session;

import module java.base;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.session.DelegatingIndexResolver;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.IndexResolver;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.PrincipalNameIndexResolver;
import org.springframework.session.Session;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is {@link TicketRegistrySessionRepository}.
 * Sessions are kept as transient tickets whose properties are all text: times as ISO-8601 instants and
 * each session attribute as serialized text under its own property, so ticket registries that only keep text
 * properties, such as the stateless ticket registry, keep them intact. After a session is saved, its id becomes
 * the id of the ticket as stored by the registry, so the session cookie carries an id the registry can find.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@Transactional(transactionManager = TicketRegistry.TICKET_TRANSACTION_MANAGER)
public class TicketRegistrySessionRepository extends MapSessionRepository implements FindByIndexNameSessionRepository<MapSession> {
    private static final String ATTRIBUTE_PROPERTY_PREFIX = "attribute.";

    private final IndexResolver<Session> indexResolver = new DelegatingIndexResolver<>(new PrincipalNameIndexResolver<>());

    private final ObjectProvider<TicketRegistry> ticketRegistry;
    private final ObjectProvider<TicketFactory> ticketFactory;

    public TicketRegistrySessionRepository(final ObjectProvider<TicketRegistry> ticketRegistry,
                                           final ObjectProvider<TicketFactory> ticketFactory) {
        super(new ConcurrentHashMap<>());
        this.ticketRegistry = ticketRegistry;
        this.ticketFactory = ticketFactory;
    }

    @Override
    public void save(final MapSession session) {
        FunctionUtils.doUnchecked(_ -> {
            if (!session.getId().equals(session.getOriginalId())) {
                deleteById(session.getOriginalId());
            }
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(session.getId());
            val storedTicket = storeSession(session, ticketId);
            if (!storedTicket.getId().equals(ticketId)) {
                LOGGER.trace("Session [{}] is stored as [{}]", ticketId, storedTicket.getId());
                session.setId(storedTicket.getId());
            }
        });
    }

    private Ticket storeSession(final MapSession session, final String ticketId) throws Exception {
        try {
            val currentTicket = ticketRegistry.getObject().getTicket(ticketId, TransientSessionTicket.class);
            currentTicket.getProperties().keySet().removeIf(name -> name.startsWith(ATTRIBUTE_PROPERTY_PREFIX));
            currentTicket.getProperties().putAll(convertSessionAttributes(session));
            LOGGER.trace("Updating session [{}] with properties [{}]", currentTicket.getId(), currentTicket);
            return Objects.requireNonNull(ticketRegistry.getObject().updateTicket(currentTicket), () -> "Unable to update session " + ticketId);
        } catch (final InvalidTicketException e) {
            val factory = (TransientSessionTicketFactory) ticketFactory.getObject().get(TransientSessionTicket.class);
            val ticket = factory.create(ticketId, convertSessionAttributes(session));
            LOGGER.trace("Saving session [{}] with properties [{}]", ticket.getId(), ticket);
            return Objects.requireNonNull(ticketRegistry.getObject().addTicket(ticket), () -> "Unable to store session " + ticketId);
        }
    }

    @Override
    public @Nullable MapSession findById(final String id) {
        try {
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(id);
            LOGGER.trace("Finding session by id [{}]", ticketId);
            val ticket = ticketRegistry.getObject().getTicket(ticketId, TransientSessionTicket.class);
            return convertTicketToSession(ticket);
        } catch (final InvalidTicketException e) {
            LOGGER.trace("Session with id [{}] not found", id, e);
        }
        return null;
    }

    private static @NonNull MapSession convertTicketToSession(final TransientSessionTicket ticket) {
        val newSession = new MapSession(ticket.getId());
        newSession.setCreationTime(toInstant(ticket.getProperties().get("creationTime")));
        newSession.setLastAccessedTime(toInstant(ticket.getProperties().get("lastAccessedTime")));
        ticket.getProperties().forEach((name, value) -> {
            if (name.startsWith(ATTRIBUTE_PROPERTY_PREFIX) && value != null) {
                val decoded = EncodingUtils.decodeBase64(value.toString());
                val attributeValue = SerializationUtils.deserialize(decoded, Serializable.class);
                newSession.setAttribute(name.substring(ATTRIBUTE_PROPERTY_PREFIX.length()), attributeValue);
            }
        });
        LOGGER.trace("Found session [{}] with attributes [{}]", newSession.getId(), newSession.getAttributeNames());
        return newSession;
    }

    private static Instant toInstant(final @Nullable Object value) {
        return switch (value) {
            case final Instant instant -> instant;
            case null -> Instant.now();
            default -> Instant.parse(value.toString());
        };
    }

    @Override
    public void deleteById(final String id) {
        FunctionUtils.doUnchecked(_ -> {
            LOGGER.trace("Deleting session by id [{}]", id);
            ticketRegistry.getObject().deleteTicket(id);
        });
    }

    @Override
    public Map findByIndexNameAndIndexValue(final String indexName, final String indexValue) {
        try (val tickets = ticketRegistry.getObject().getTickets(ticket -> ticket instanceof final TransientSessionTicket tst
            && indexValue.equals(tst.getProperty(indexName, String.class)))) {
            return tickets
                .map(TransientSessionTicket.class::cast)
                .map(TicketRegistrySessionRepository::convertTicketToSession)
                .collect(Collectors.toMap(MapSession::getId, Function.identity()));
        }
    }

    private Map<String, Object> convertSessionAttributes(final MapSession session) {
        val properties = new LinkedHashMap<String, Object>();
        properties.put("lastAccessedTime", session.getLastAccessedTime().toString());
        properties.put("creationTime", session.getCreationTime().toString());
        properties.put("originalId", session.getOriginalId());
        properties.put("id", session.getId());

        val principalName = this.indexResolver
            .resolveIndexesFor(session)
            .get(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
        properties.put(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, principalName);

        session.getAttributeNames().forEach(name -> {
            val value = (Serializable) session.getAttribute(name);
            if (value != null) {
                properties.put(ATTRIBUTE_PROPERTY_PREFIX + name, SerializationUtils.serializeBase64(value));
            }
        });
        return properties;
    }
}
