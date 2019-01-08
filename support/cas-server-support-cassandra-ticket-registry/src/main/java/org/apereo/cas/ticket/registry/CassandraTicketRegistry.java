package org.apereo.cas.ticket.registry;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.ticket.BaseTicketSerializers;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.datastax.driver.mapping.annotations.QueryParameters;

public class CassandraTicketRegistry extends AbstractTicketRegistry {

    private final Mapper<TicketHolder> entityManager;

    private final TicketAccessor ticketAccessor;

    private final TicketCatalog ticketCatalog;

    @Accessor
    interface TicketAccessor {
        @Query("SELECT id, data FROM ticket")
        @QueryParameters(consistency = "ONE")
        Result<TicketHolder> findAll();
    }

    public CassandraTicketRegistry(TicketCatalog ticketCatalog, CassandraSessionFactory cassandraSessionFactory) {
        this.ticketCatalog = ticketCatalog;
        MappingManager manager = new MappingManager(cassandraSessionFactory.getSession());
        entityManager = manager.mapper(TicketHolder.class);
        ticketAccessor = manager.createAccessor(TicketAccessor.class);
    }

    @Override
    public Ticket getTicket(String ticketId) {
        TicketHolder holder = entityManager.get(ticketId);
        return Optional.ofNullable(holder)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        Ticket found = getTicket(ticketId);
        if (null == found || !predicate.test(found)) {
            return null;
        }
        return found;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return ticketAccessor.findAll().all().stream()
                .map(this::deserialize)
                .collect(Collectors.toList());
    }

    @Override
    public void addTicket(Ticket ticket) {
        findMetadata(ticket);
        String data = BaseTicketSerializers.serializeTicket(ticket);
        Mapper.Option ttl = getTtl(ticket);
        entityManager.save(new TicketHolder(ticket.getId(), data), ttl);
    }

    @Override
    public Ticket updateTicket(Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public boolean deleteSingleTicket(String ticketId) {
        entityManager.delete(ticketId);
        return true;
    }

    @Override
    public long deleteAll() {
        Collection<Ticket> tickets = getTickets();
        tickets.forEach(ticket -> deleteSingleTicket(ticket.getId()));
        return tickets.size();
    }

    private Ticket deserialize(TicketHolder holder) {
        TicketDefinition metadata = findMetadata(holder.getId());
        return BaseTicketSerializers.deserializeTicket(holder.getData(), metadata.getImplementationClass());
    }

    private TicketDefinition findMetadata(Ticket ticket) {
        return findMetadata(ticket.getId());
    }

    private TicketDefinition findMetadata(String id) {
        return Optional.ofNullable(ticketCatalog.find(id)).orElseThrow(() -> new IllegalStateException(
                "Ticket catalog has no metadata for " + id.replaceAll("-.*", "") + " tickets"
        ));
    }

    private Mapper.Option getTtl(Ticket ticket) {
        ExpirationPolicy expirationPolicy = ticket.getExpirationPolicy();
        return Mapper.Option.ttl(Math.toIntExact(expirationPolicy.getTimeToIdle()));
    }

}
