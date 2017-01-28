package org.apereo.cas.ticket.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
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
    
    private IMap<String, Ticket> registry;

    private HazelcastInstance hazelcastInstance;

    private int pageSize;

    /**
     * Instantiates a new Hazelcast ticket registry.
     *
     * @param hz       An instance of {@code HazelcastInstance}
     * @param mapName  Name of map to use
     * @param pageSize the page size
     */
    public HazelcastTicketRegistry(final HazelcastInstance hz, final String mapName, final int pageSize) {
        this.registry = hz.getMap(mapName);
        this.hazelcastInstance = hz;
        this.pageSize = pageSize;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        LOGGER.info("Setting up Hazelcast Ticket Registry instance [{}] with name [{}]", this.hazelcastInstance, registry.getName());
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
        this.registry.set(encTicket.getId(), encTicket, ticket.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
    }


    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isNotBlank(encTicketId)) {
            final Ticket ticket = this.registry.get(encTicketId);
            return decodeTicket(ticket);
        }
        return null;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        return this.registry.remove(ticketId) != null;
    }

    @Override
    public long deleteAll() {
        final int size = this.registry.size();
        this.registry.evictAll();
        this.registry.clear();
        return size;
    }
    
    @Override
    public Collection<Ticket> getTickets() {
        final Collection<Ticket> collection = new HashSet<>();

        LOGGER.debug("Attempting to acquire lock from Hazelcast instance...");
        final Lock lock = this.hazelcastInstance.getLock(getClass().getName());
        lock.lock();
        LOGGER.debug("Hazelcast instance lock acquired");

        try {
            LOGGER.debug("Setting up the paging predicate with page size of [{}]", this.pageSize);
            final PagingPredicate pagingPredicate = new PagingPredicate(this.pageSize);

            LOGGER.debug("Retrieving the initial collection of tickets from Hazelcast instance...");
            Collection<Ticket> entrySet = this.registry.values(pagingPredicate);

            while (!entrySet.isEmpty()) {
                collection.addAll(entrySet.stream().map(this::decodeTicket).collect(Collectors.toList()));

                pagingPredicate.nextPage();
                entrySet = this.registry.values(pagingPredicate);
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        return collection;
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
}
