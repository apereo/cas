package org.apereo.cas.ticket.registry;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
    
    private IMap<String, Ticket> tgts;

    private IMap<String, Ticket> sts;

    private IMap<String, Set<String>> users;

    private HazelcastInstance hazelcastInstance;

    private int pageSize;

    private final String ticketTypeRegEx = "^(" + TicketGrantingTicket.PREFIX + "-" +
                                           "|" + ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX + "-" +
                                           "|" + ProxyGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX + "-" +
                                           ").*$";

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

        this.tgts = hz.getMap(mapName);
        this.sts = hz.getMap("service_tickets");
        this.users = hz.getMap("users");
        this.hazelcastInstance = hz;
        this.pageSize = pageSize;

        /**
         * Add MapListeners to update user map
         */
        this.tgts.addLocalEntryListener(new EntryRemovedListener<String,Ticket>() {
            @Override
            public void entryRemoved(EntryEvent<String,Ticket> entryEvent) {
                String user = ((TicketGrantingTicket)entryEvent.getOldValue()).getAuthentication().getPrincipal().getId();
                removeTGTfromUser(user,entryEvent.getKey());
            }
        });
        this.tgts.addLocalEntryListener(new EntryExpiredListener<String,Ticket>() {
            @Override
            public void entryExpired(EntryEvent<String,Ticket> entryEvent) {
                String user = ((TicketGrantingTicket)entryEvent.getOldValue()).getAuthentication().getPrincipal().getId();
                removeTGTfromUser(user,entryEvent.getKey());
            }
        });
        this.tgts.addLocalEntryListener(new EntryAddedListener<String,Ticket>() {
            @Override
            public void entryAdded(EntryEvent<String, Ticket> entryEvent) {
                String user = ((TicketGrantingTicket)entryEvent.getValue()).getAuthentication().getPrincipal().getId();
                addTGTtoUser(user,entryEvent.getKey());
            }
        });

    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        LOGGER.info("Setting up Hazelcast Ticket Registry instance [{}] with name [{}]", this.hazelcastInstance, tgts.getName());

    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(), ticket.getExpirationPolicy().getTimeToLive());
        if (ticket instanceof TicketGrantingTicket) {
            addTGT(ticket);
        } else if (ticket instanceof ServiceTicket) {
            addST(ticket);
        } else {
            throw new IllegalArgumentException(
                    String.format("Invalid ticket type [%s]. Expecting either [TicketGrantingTicket] or [ServiceTicket]",
                            ticket.getClass().getName()));
        }
    }

    /**
     * Adds the ticket to the hazelcast instance.
     *
     * @param ticket a ticket
     */
    private void addTGT(Ticket ticket) {
        LOGGER.debug("Adding TGT [{}] with ttl [{}s]", ticket.getId(), ticket.getExpirationPolicy().getTimeToLive());
        final Ticket encTicket = encodeTicket(ticket);
        this.tgts.set(encTicket.getId(), encTicket, ticket.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
    }

    /**
     * Adds the ticket to the hazelcast instance.
     *
     * @param ticket a ticket
     */
    private void addST(final Ticket ticket) {
        LOGGER.debug("Adding  ST [{}] with ttl [{}s]", ticket.getId(), ticket.getExpirationPolicy().getTimeToLive());
        final Ticket encTicket = encodeTicket(ticket);
        this.sts.set(encTicket.getId(), encTicket, ticket.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return get(ticketId, ticketId.matches(ticketTypeRegEx) ? this.tgts : this.sts);
    }

    private Ticket get(final String ticketId, IMap<String,Ticket> map) {
        final String encTicketId = encodeTicketId(ticketId);
        final Ticket ticket = decodeTicket(map.get(encTicketId));
        return ticket;
        }

    private Ticket remove(final String ticketId, IMap<String,Ticket> map) {
        String encTicketId = encodeTicketId(ticketId);
        return map.remove(encTicketId);
    }

    private void addTGTtoUser(String user, String ticketId) {
        String encodedUser = encodeTicketId(user);
        String encodedTicketId = encodeTicketId(ticketId);
        Set<String> tgtSet = this.users.get(encodedUser);
        if (tgtSet == null) {
            tgtSet = new HashSet<>();
        }
        tgtSet.add(encodedTicketId);
        LOGGER.info("Added tgt [{}] to user [{}], tgts size now [{}]",ticketId,user,tgtSet.size());
        this.users.set(encodedUser,tgtSet);
    }

    private void removeTGTfromUser(String user, String ticketId) {
        String encodedUser = encodeTicketId(user);
        String encodedTicketId = encodeTicketId(ticketId);
        Set<String> tgtSet = this.users.get(encodedUser);
        if(tgtSet != null && !tgtSet.isEmpty()) {
            tgtSet.remove(encodedTicketId);
            LOGGER.info("Removed tgt [{}] from user [{}], tgt size now [{}]",ticketId,user,tgtSet.size());
            if (tgtSet.isEmpty()) {
                this.users.remove(encodedUser);
            } else {
                this.users.set(encodedUser,tgtSet);
            }
        }
    }


    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        if (ticketId.matches(ticketTypeRegEx)) {
            LOGGER.debug("Removing ticket [{}] from the TGT tgts.", ticketId);
            return remove(ticketId,this.tgts) != null;
        } else {
            LOGGER.debug("Removing ticket [{}] from the ST tgts.", ticketId);
            return remove(ticketId,this.sts) != null;
        }
    }

    @Override
    public long deleteAll() {
        final long size = this.tgts.size();
        this.tgts.evictAll();
        this.tgts.clear();
        this.sts.evictAll();
        this.sts.clear();
        return size;
    }
    
    @Override
    public long sessionCount() {
        return this.tgts.size();
    }

    @Override
    public long serviceTicketCount() {
        return this.sts.size();
    }

    @Override
    public long userCount() {
        return this.users.size();
    }


    @Override
    public Collection<Ticket> getTickets() {
        return tgts.values().stream().limit(100).collect(Collectors.toList());
    }

    public Collection<Ticket> getTicketsByUser(String user) {
        final Collection<Ticket> collection = new HashSet<>();

        users.keySet(key -> ((String)key.getKey()).matches(user)).stream().limit(100).forEach(usr -> {
            users.get(usr).forEach(tgt -> collection.add(tgts.get(tgt)));
        });

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

    public void setRegistry(final IMap<String, Ticket> registry) {
        this.tgts = registry;
    }

    public void setHazelcastInstance(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }

}
