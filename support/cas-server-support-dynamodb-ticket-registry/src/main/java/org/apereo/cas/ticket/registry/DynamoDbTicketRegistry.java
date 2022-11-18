package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jooq.lambda.Unchecked;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DynamoDbTicketRegistry extends AbstractTicketRegistry {
    private final DynamoDbTicketRegistryFacilitator dbTableService;

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        return this.dbTableService.getSessionsFor(encodeTicketId(principalId));
    }

    @Override
    public void addTicket(final Stream<? extends Ticket> toSave) throws Exception {
        try {
            val toPut = toSave.map(Unchecked.function(ticket -> {
                val encTicket = encodeTicket(ticket);
                val principal = encodeTicketId(getPrincipalIdFrom(ticket));
                return Triple.<Ticket, Ticket, String>of(ticket, encTicket, principal);
            }));
            dbTableService.put(toPut);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(),
                ticket.getExpirationPolicy().getTimeToLive());
            val encTicket = encodeTicket(ticket);
            val principal = encodeTicketId(getPrincipalIdFrom(ticket));
            this.dbTableService.put(ticket, encTicket, principal);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            return null;
        }
        LOGGER.debug("Retrieving ticket [{}]", ticketId);
        val ticket = dbTableService.get(ticketId, encTicketId);
        val decodedTicket = decodeTicket(ticket);
        if (decodedTicket != null && predicate.test(decodedTicket)) {
            return decodedTicket;
        }
        return null;
    }

    @Override
    public long deleteAll() {
        return dbTableService.deleteAll();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return decodeTickets(dbTableService.getAll());
    }

    @Override
    public Stream<? extends Ticket> stream() {
        return dbTableService.stream().map(this::decodeTicket);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public long deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        return dbTableService.delete(ticketIdToDelete, ticketId) ? 1 : 0;
    }

    @Override
    public long sessionCount() {
        return dbTableService.countTickets(TicketGrantingTicket.class, TicketGrantingTicket.PREFIX);
    }

    @Override
    public long serviceTicketCount() {
        return dbTableService.countTickets(ServiceTicket.class, ServiceTicket.PREFIX);
    }
}
