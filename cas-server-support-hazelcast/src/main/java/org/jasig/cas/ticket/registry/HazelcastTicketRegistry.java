package org.jasig.cas.ticket.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

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
@RefreshScope
@Component("hazelcastTicketRegistry")
public class HazelcastTicketRegistry extends AbstractTicketRegistry {

    private IMap<String, Ticket> registry;
    
    private HazelcastInstance hazelcastInstance;

    private int pageSize;

    /**
     * Instantiates a new Hazelcast ticket registry.
     */
    public HazelcastTicketRegistry() {}
    
    /**
     * Instantiates a new Hazelcast ticket registry.
     *
     * @param hz       An instance of {@code HazelcastInstance}
     * @param mapName  Name of map to use
     * @param pageSize the page size
     */
    @Autowired
    public HazelcastTicketRegistry(
        @Qualifier("hazelcast")
        final HazelcastInstance hz,
        @Value("${hz.mapname:tickets}")
        final String mapName,
        @Value("${hz.page.size:500}")
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
    protected void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    @Override
    protected boolean needsCallback() {
        return false;
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
        final Ticket ticket = decodeTicket(this.registry.get(encTicketId));
        return getProxiedTicketInstance(ticket);
    }
    
    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        return this.registry.remove(ticketId) != null;
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
                for (final Ticket entry : entrySet) {
                    collection.add(decodeTicket(entry));
                }
            }
        } finally {
            lock.unlock();
        }
        return collection;
    }
    
    @Override
    public long sessionCount() {
        return getTickets().stream().filter(t -> t instanceof TicketGrantingTicket).count();
    }

    @Override
    public long serviceTicketCount() {
        return getTickets().stream().filter(t -> t instanceof ServiceTicket).count();
    }

    /**
     * Make sure we shutdown HazelCast when the context is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Hazelcast instance {}", this.hazelcastInstance.getConfig().getInstanceName());
        this.hazelcastInstance.shutdown();
    }
    
    public void setRegistry(final IMap<String, Ticket> registry) {
        this.registry = registry;
    }

    public void setHazelcastInstance(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
