package org.apereo.cas.ticket.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
public class HazelcastTicketRegistry extends AbstractTicketRegistry implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastTicketRegistry.class);

    private final HazelcastInstance hazelcastInstance;
    private final TicketCatalog ticketCatalog;
    private final int pageSize;

    /**
     * Instantiates a new Hazelcast ticket ticketGrantingTicketsRegistry.
     *
     * @param hz       An instance of {@code HazelcastInstance}
     * @param plan     the plan
     * @param pageSize the page size
     */
    public HazelcastTicketRegistry(final HazelcastInstance hz, final TicketCatalog plan, final int pageSize) {
        this.hazelcastInstance = hz;
        this.pageSize = pageSize;
        this.ticketCatalog = plan;

        LOGGER.info("Setting up Hazelcast Ticket Registry instance [{}]", this.hazelcastInstance);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        final long ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl < 0) {
            throw new IllegalArgumentException("The expiration policy of ticket " + ticket.getId() + "is set to use a negative ttl");
        }

        LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(), ttl);
        final Ticket encTicket = encodeTicket(ticket);

        final TicketDefinition metadata = this.ticketCatalog.find(ticket);
        final IMap<String, Ticket> ticketMap = getTicketMapInstanceByMetadata(metadata);

        ticketMap.set(encTicket.getId(), encTicket, ttl, TimeUnit.SECONDS);
        LOGGER.debug("Added ticket [{}] with ttl [{}s]", encTicket.getId(), ttl);
    }

    private IMap<String, Ticket> getTicketMapInstanceByMetadata(final TicketDefinition metadata) {
        final String mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating map name [{}] for ticket definition [{}]", mapName, metadata);
        return getTicketMapInstance(mapName);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isNotBlank(encTicketId)) {
            final TicketDefinition metadata = this.ticketCatalog.find(ticketId);
            if (metadata != null) {
                final IMap<String, Ticket> map = getTicketMapInstanceByMetadata(metadata);
                final Ticket ticket = map.get(encTicketId);
                final Ticket result = decodeTicket(ticket);
                if (result != null && result.isExpired()) {
                    LOGGER.debug("Ticket [{}] has expired and is now removed from the cache", result.getId());
                    map.remove(encTicketId);
                    return null;
                }
                return result;
            }
            LOGGER.warn("No ticket definition could be found in the catalog to match [{}]", ticketId);
        }
        return null;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        final String encTicketId = encodeTicketId(ticketIdToDelete);
        final TicketDefinition metadata = this.ticketCatalog.find(ticketIdToDelete);
        final IMap<String, Ticket> map = getTicketMapInstanceByMetadata(metadata);
        return map.remove(encTicketId) != null;
    }

    @Override
    public long deleteAll() {
        return this.ticketCatalog.findAll().stream()
                .map(this::getTicketMapInstanceByMetadata)
                .filter(Objects::nonNull)
                .mapToInt(instance -> {
                    final int size = instance.size();
                    instance.evictAll();
                    instance.clear();
                    return size;
                })
                .sum();
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
                .map(metadata -> getTicketMapInstanceByMetadata(metadata).values())
                .flatMap(tickets -> tickets.stream().limit(pageSize).collect(Collectors.toList()).stream())
                .map(this::decodeTicket)
                .collect(Collectors.toSet());
    }

    /**
     * Make sure we shutdown HazelCast when the context is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        try {
            LOGGER.info("Shutting down Hazelcast instance [{}]", this.hazelcastInstance.getConfig().getInstanceName());
            this.hazelcastInstance.shutdown();
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage());
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    private IMap<String, Ticket> getTicketMapInstance(final String mapName) {
        try {
            final IMap<String, Ticket> inst = hazelcastInstance.getMap(mapName);
            LOGGER.debug("Located Hazelcast map instance [{}]", mapName);
            return inst;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
