package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Hazelcast-based implementation of a {@link TicketRegistry}.
 * <p>This implementation just wraps the Hazelcast's {@link IMap}
 * which is an extension of the standard Java's {@code ConcurrentMap}.</p>
 * <p>The heavy lifting of distributed data partitioning, network cluster discovery and
 * join, data replication, etc. is done by Hazelcast's Map implementation.</p>
 *
 * @author Dmitriy Kopylenko
 * @author Jonathan Johnson
 * @since 4.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class HazelcastTicketRegistry extends AbstractTicketRegistry implements AutoCloseable, DisposableBean {
    private final HazelcastInstance hazelcastInstance;
    private final TicketCatalog ticketCatalog;
    private final long pageSize;

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl < 0) {
            throw new IllegalArgumentException("The expiration policy of ticket " + ticket.getId() + "is set to use a negative ttl");
        }

        LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(), ttl);
        val encTicket = encodeTicket(ticket);

        val metadata = this.ticketCatalog.find(ticket);
        val ticketMap = getTicketMapInstanceByMetadata(metadata);

        ticketMap.set(encTicket.getId(), encTicket, ttl, TimeUnit.SECONDS);
        LOGGER.debug("Added ticket [{}] with ttl [{}s]", encTicket.getId(), ttl);
    }

    private IMap<String, Ticket> getTicketMapInstanceByMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating map name [{}] for ticket definition [{}]", mapName, metadata);
        return getTicketMapInstance(mapName);
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            return null;
        }
        val metadata = this.ticketCatalog.find(ticketId);
        if (metadata != null) {
            val map = getTicketMapInstanceByMetadata(metadata);
            val ticket = map.get(encTicketId);
            val result = decodeTicket(ticket);
            if (predicate.test(result)) {
                return result;
            }
            return null;
        }
        LOGGER.warn("No ticket definition could be found in the catalog to match [{}]", ticketId);
        return null;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val encTicketId = encodeTicketId(ticketIdToDelete);
        val metadata = this.ticketCatalog.find(ticketIdToDelete);
        val map = getTicketMapInstanceByMetadata(metadata);
        return map.remove(encTicketId) != null;
    }

    @Override
    public long deleteAll() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketMapInstanceByMetadata)
            .filter(Objects::nonNull)
            .mapToInt(instance -> {
                val size = instance.size();
                instance.evictAll();
                instance.clear();
                return size;
            })
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll()
            .stream()
            .map(metadata -> getTicketMapInstanceByMetadata(metadata).values())
            .flatMap(tickets -> {
                if (pageSize > 0) {
                    return tickets.stream().limit(pageSize).collect(Collectors.toList()).stream();
                }
                return new ArrayList<>(tickets).stream();
            })
            .map(this::decodeTicket)
            .collect(Collectors.toSet());
    }

    /**
     * Make sure we shutdown HazelCast when the context is destroyed.
     */
    public void shutdown() {
        try {
            LOGGER.info("Shutting down Hazelcast instance [{}]", this.hazelcastInstance.getConfig().getInstanceName());
            this.hazelcastInstance.shutdown();
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage());
        }
    }

    @Override
    public void destroy() throws Exception {
        close();
    }

    @Override
    public void close() {
        shutdown();
    }

    private IMap<String, Ticket> getTicketMapInstance(final String mapName) {
        try {
            val inst = hazelcastInstance.<String, Ticket>getMap(mapName);
            LOGGER.debug("Located Hazelcast map instance [{}]", mapName);
            return inst;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
