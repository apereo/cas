package org.apereo.cas.ticket.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataRegistrationPlan;
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
    /**
     * Hazelcast map name in the ticket metadata.
     */
    public static final String HAZELCAST_PROPERTY_NAME_MAP = "hazelcastMapName";

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastTicketRegistry.class);

    private final HazelcastInstance hazelcastInstance;
    private final TicketMetadataRegistrationPlan ticketMetadataRegistrationPlan;
    private final int pageSize;

    /**
     * Instantiates a new Hazelcast ticket ticketGrantingTicketsRegistry.
     *
     * @param hz       An instance of {@code HazelcastInstance}
     * @param pageSize the page size
     */
    public HazelcastTicketRegistry(final HazelcastInstance hz, final TicketMetadataRegistrationPlan plan, final int pageSize) {
        this.hazelcastInstance = hz;
        this.pageSize = pageSize;
        this.ticketMetadataRegistrationPlan = plan;
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

        final TicketMetadata metadata = this.ticketMetadataRegistrationPlan.findTicketMetadata(ticket);
        final IMap<String, Ticket> ticketMap = getTicketMapInstanceByMetadata(metadata);
        ticketMap.set(encTicket.getId(), encTicket, ticket.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
    }

    private IMap<String, Ticket> getTicketMapInstanceByMetadata(final TicketMetadata metadata) {
        final String mapName = metadata.getPropertyAsString(HAZELCAST_PROPERTY_NAME_MAP);
        return getTicketMapInstance(mapName);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isNotBlank(encTicketId)) {
            final TicketMetadata metadata = this.ticketMetadataRegistrationPlan.findTicketMetadata(ticketId);
            final Ticket ticket = getTicketMapInstanceByMetadata(metadata).get(encTicketId);
            return decodeTicket(ticket);
        }
        return null;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final TicketMetadata metadata = this.ticketMetadataRegistrationPlan.findTicketMetadata(ticketId);
        return getTicketMapInstanceByMetadata(metadata).remove(ticketId) != null;
    }

    @Override
    public long deleteAll() {
        final Collection<TicketMetadata> metadata = this.ticketMetadataRegistrationPlan.findAllTicketMetadata();
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
            final TicketMetadata tgts = this.ticketMetadataRegistrationPlan.findTicketMetadata(TicketGrantingTicket.PREFIX);
            final TicketMetadata sts = this.ticketMetadataRegistrationPlan.findTicketMetadata(ServiceTicket.PREFIX);

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
        final TicketMetadata metadata = this.ticketMetadataRegistrationPlan.findTicketMetadata(TicketGrantingTicket.PREFIX);
        return getTicketMapInstanceByMetadata(metadata).size();
    }

    @Override
    public long serviceTicketCount() {
        final TicketMetadata metadata = this.ticketMetadataRegistrationPlan.findTicketMetadata(ServiceTicket.PREFIX);
        return getTicketMapInstanceByMetadata(metadata).size();
    }

    private IMap<String, Ticket> getTicketMapInstance(final String mapName) {
        try {
            return hazelcastInstance.getMap(mapName);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
