package org.jasig.cas.ticket.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.jasig.cas.ticket.Ticket;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

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
@Component("hazelcastTicketRegistry")
public class HazelcastTicketRegistry extends AbstractTicketRegistry implements DisposableBean {

    private final IMap<String, Ticket> registry;
    
    private final HazelcastInstance hz;


    /**
     * @param hz                                  An instance of {@code HazelcastInstance}
     * @param mapName                             Name of map to use
     */
    @Autowired
    public HazelcastTicketRegistry(
        @Qualifier("hazelcast")
        final HazelcastInstance hz,
        @Value("${hz.mapname:tickets}")
        final String mapName) {

        this.registry = hz.getMap(mapName);
        this.hz = hz;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up Hazelcast Ticket Registry...");
        logger.debug("Hazelcast instance: {} with name {}", this.hz, this.registry.getName());
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
        return decodeTickets(this.registry.values());
    }

    /**
     * Make sure we shutdown HazelCast when the context is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Hazelcast instance {}", hz.getConfig().getInstanceName());
        this.hz.shutdown();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }
}
