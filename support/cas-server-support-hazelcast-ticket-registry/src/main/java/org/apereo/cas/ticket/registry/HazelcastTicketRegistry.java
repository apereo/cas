package org.apereo.cas.ticket.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import org.apereo.cas.ticket.Ticket;

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

    private IMap<String, Ticket> registry;

    private HazelcastInstance hazelcastInstance;

    private int pageSize;

    /**
     * Instantiates a new Hazelcast ticket registry.
     */
    public HazelcastTicketRegistry() {
    }

    /**
     * Instantiates a new Hazelcast ticket registry.
     *
     * @param hz       An instance of {@code HazelcastInstance}
     * @param mapName  Name of map to use
     * @param pageSize the page size
     */
    public HazelcastTicketRegistry(
            final HazelcastInstance hz,
            final String mapName,
            final int pageSize) {

        this.registry = hz.getMap(mapName);
        this.hazelcastInstance = hz;
        this.pageSize = pageSize;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up Hazelcast Ticket Registry instance {} with name {}",
                this.hazelcastInstance, this.registry.getName());
    }

    @Override
    public void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }
    
    @Override
    public void addTicket(final Ticket ticket) {
        logger.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(), ticket.getExpirationPolicy().getTimeToLive());
        final Ticket encTicket = encodeTicket(ticket);
        this.registry.set(encTicket.getId(), encTicket, ticket.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
    }


    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        return decodeTicket(this.registry.get(encTicketId));
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        return this.registry.remove(encTicketId) != null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<Ticket> collection = new HashSet<>();

        final Lock lock = this.hazelcastInstance.getLock(getClass().getName());
        lock.lock();
        try {
            final PagingPredicate pagingPredicate = new PagingPredicate(this.pageSize);
            for (Collection<Ticket> entrySet = this.registry.values(pagingPredicate);
                 !entrySet.isEmpty();
                 pagingPredicate.nextPage(), entrySet = this.registry.values(pagingPredicate)) {
                collection.addAll(entrySet.stream().map(this::decodeTicket).collect(Collectors.toList()));
            }
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
        logger.info("Shutting down Hazelcast instance {}", this.hazelcastInstance.getConfig().getInstanceName());
        try {
            this.hazelcastInstance.shutdown();
        } catch (final Throwable e) {
            logger.debug(e.getMessage());
        }
    }

    public void setRegistry(final IMap<String, Ticket> registry) {
        this.registry = registry;
    }

    public void setHazelcastInstance(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }
}
