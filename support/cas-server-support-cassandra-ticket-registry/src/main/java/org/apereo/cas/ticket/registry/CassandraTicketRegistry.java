package org.apereo.cas.ticket.registry;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.ticketregistry.CassandraTicketRegistryProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link CassandraTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @author doomviking
 * @since 6.1.0
 */
@Slf4j
public class CassandraTicketRegistry extends AbstractTicketRegistry implements DisposableBean {
    private final Mapper<CassandraTicketHolder> entityManager;
    private final TicketCatalog ticketCatalog;
    private final CassandraTicketRegistryProperties properties;
    private final Session cassandraSession;
    private final TicketSerializationManager ticketSerializationManager;

    public CassandraTicketRegistry(final TicketCatalog ticketCatalog,
                                   final CassandraSessionFactory cassandraSessionFactory,
                                   final CassandraTicketRegistryProperties properties,
                                   final TicketSerializationManager ticketSerializationManager) {
        this.ticketCatalog = ticketCatalog;
        this.properties = properties;

        this.cassandraSession = cassandraSessionFactory.getSession();
        val mappingManager = new MappingManager(this.cassandraSession);
        this.entityManager = mappingManager.mapper(CassandraTicketHolder.class);

        this.ticketSerializationManager = ticketSerializationManager;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val definition = ticketCatalog.find(ticketId);
        if (definition == null) {
            LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
            return null;
        }

        LOGGER.trace("Locating ticket ticketId [{}]", ticketId);
        val encodedTicketId = encodeTicketId(ticketId);
        val holder = entityManager.get(encodedTicketId);
        if (holder == null) {
            LOGGER.debug("Ticket id [{}] could not be found", ticketId);
            return null;
        }

        val result = decodeTicket(deserialize(holder));
        if (result != null && predicate.test(result)) {
            return result;
        }
        LOGGER.trace("The condition enforced by the predicate [{}] cannot successfully accept/test the ticket id [{}]", ticketId,
            predicate.getClass().getSimpleName());
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        val query = String.format("SELECT id, data FROM %s", CassandraTicketHolder.TABLE_NAME);
        val results = cassandraSession.execute(query);
        val mappedResults = entityManager.map(results);
        return mappedResults
            .all()
            .stream()
            .map(this::deserialize)
            .map(this::decodeTicket)
            .collect(Collectors.toList());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        val encTicket = encodeTicket(ticket);
        val data = ticketSerializationManager.serializeTicket(encTicket);
        val ttl = getTimeToLive(ticket);
        entityManager.save(new CassandraTicketHolder(encTicket.getId(), data), ttl, getConsistencyLevel());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val encTicketId = encodeTicketId(ticketId);
        entityManager.delete(encTicketId, getConsistencyLevel());
        return true;
    }

    @Override
    public long deleteAll() {
        val count = new AtomicLong();
        getTicketsStream().forEach(ticket -> {
            deleteSingleTicket(ticket.getId());
            count.incrementAndGet();
        });
        return count.longValue();
    }

    @Override
    public void destroy() {
        this.cassandraSession.close();
    }

    private Ticket deserialize(final CassandraTicketHolder holder) {
        return ticketSerializationManager.deserializeTicket(holder.getData(), EncodedTicket.class);
    }

    private static Mapper.Option getTimeToLive(final Ticket ticket) {
        val expirationPolicy = ticket.getExpirationPolicy();
        return Mapper.Option.ttl(Math.toIntExact(expirationPolicy.getTimeToLive()));
    }

    private Mapper.Option getConsistencyLevel() {
        return Mapper.Option.consistencyLevel(ConsistencyLevel.valueOf(properties.getConsistencyLevel()));
    }

}
