package org.apereo.cas.ticket.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
     * @param pageSize the page size
     */
    public HazelcastTicketRegistry(final HazelcastInstance hz, final TicketCatalog plan, final int pageSize) {
        this.hazelcastInstance = hz;
        this.pageSize = pageSize;
        this.ticketCatalog = plan;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        LOGGER.info("Setting up Hazelcast Ticket Registry instance [{}] with name [{}]", this.hazelcastInstance);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(), ticket.getExpirationPolicy().getTimeToLive());
        final Ticket encTicket = encodeTicket(ticket);

        final TicketDefinition metadata = this.ticketCatalog.find(ticket);
        final IMap<String, Ticket> ticketMap = getTicketMapInstanceByMetadata(metadata);
        ticketMap.set(encTicket.getId(), encTicket, ticket.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
    }

    private IMap<String, Ticket> getTicketMapInstanceByMetadata(final TicketDefinition metadata) {
        final String mapName = metadata.getProperties().getCacheName();
        LOGGER.debug("Locating map name [{}] for ticket definition [{}]", metadata);
        return getTicketMapInstance(mapName);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isNotBlank(encTicketId)) {
            final TicketDefinition metadata = this.ticketCatalog.find(ticketId);
            if (metadata != null) {
                final Ticket ticket = getTicketMapInstanceByMetadata(metadata).get(encTicketId);
                return decodeTicket(ticket);
            } else {
                LOGGER.warn("No ticket definition could be found in the catalog to match [{}]", ticketId);
            }
        }
        return null;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final TicketDefinition metadata = this.ticketCatalog.find(ticketId);
        return getTicketMapInstanceByMetadata(metadata).remove(ticketId) != null;
    }

    @Override
    public long deleteAll() {
        final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
        final AtomicLong count = new AtomicLong();
        metadata.forEach(r -> {
            final IMap<String, Ticket> instance = getTicketMapInstanceByMetadata(r);
            if (instance != null) {
                count.addAndGet(instance.size());
                instance.evictAll();
                instance.clear();
            }
        });
        return count.get();
    }

    @Override
    public Collection<Ticket> getTickets() {
        Collection<Ticket> tickets = new HashSet<>();

        try {
            final TicketDefinition tgts = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
            final TicketDefinition sts = this.ticketCatalog.find(ServiceTicket.PREFIX);

            tickets = getTicketMapInstanceByMetadata(tgts).values().stream().limit(this.pageSize).collect(Collectors.toList());
            tickets.addAll(getTicketMapInstanceByMetadata(sts).values());
            return tickets;
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return tickets;
    }

    /**
     * Make sure we shutdown HazelCast when the context is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        try {
            LOGGER.info("Shutting down Hazelcast instance [{}]", this.hazelcastInstance.getConfig().getInstanceName());
            this.hazelcastInstance.shutdown();
        } catch (final Throwable e) {
            LOGGER.debug(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }

    @Override
    public long sessionCount() {
        final TicketDefinition metadata = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        return getTicketMapInstanceByMetadata(metadata).size();
    }

    @Override
    public long serviceTicketCount() {
        final TicketDefinition metadata = this.ticketCatalog.find(ServiceTicket.PREFIX);
        return getTicketMapInstanceByMetadata(metadata).size();
    }

    private IMap<String, Ticket> getTicketMapInstance(final String mapName) {
        try {
            final IMap<String, Ticket> inst = hazelcastInstance.getMap(mapName);
            LOGGER.debug("Located Hazelcast map instance [{}] for [{}]", inst, mapName);
            return inst;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
