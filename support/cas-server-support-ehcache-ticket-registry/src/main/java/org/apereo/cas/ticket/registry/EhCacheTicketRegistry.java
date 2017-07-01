package org.apereo.cas.ticket.registry;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * <p>
 * <a href="http://ehcache.org/">Ehcache</a> based distributed ticket registry.
 * </p>
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Adam Rybicki
 * @author Andrew Tillinghast
 * @since 3.5
 */
public class EhCacheTicketRegistry extends AbstractTicketRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(EhCacheTicketRegistry.class);

    private final TicketCatalog ticketCatalog;

    private final CacheManager cacheManager;

    /**
     * Instantiates a new EhCache ticket registry.
     *
     * @param ticketCatalog the ticket catalog
     * @param cacheManager  the cache manager
     * @param cipher        the cipher
     */
    public EhCacheTicketRegistry(final TicketCatalog ticketCatalog, final CacheManager cacheManager,
                                 final CipherExecutor cipher) {
        this.ticketCatalog = ticketCatalog;
        this.cacheManager = cacheManager;
        setCipherExecutor(cipher);
        LOGGER.info("Setting up Ehcache Ticket Registry...");
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final TicketDefinition metadata = this.ticketCatalog.find(ticketToAdd);

        final Ticket ticket = encodeTicket(ticketToAdd);
        final Element element = new Element(ticket.getId(), ticket);

        int idleValue = ticketToAdd.getExpirationPolicy().getTimeToIdle().intValue();
        if (idleValue <= 0) {
            idleValue = ticketToAdd.getExpirationPolicy().getTimeToLive().intValue();
        }
        if (idleValue <= 0) {
            idleValue = Integer.MAX_VALUE;
        }
        element.setTimeToIdle(idleValue);

        int aliveValue = ticketToAdd.getExpirationPolicy().getTimeToLive().intValue();
        if (aliveValue <= 0) {
            aliveValue = Integer.MAX_VALUE;
        }
        element.setTimeToLive(aliveValue);
        final Ehcache cache = getTicketCacheFor(metadata);
        LOGGER.debug("Adding ticket [{}] to the cache [{}] to live [{}] seconds and stay idle for [{}] seconds",
                ticket.getId(), cache.getName(), aliveValue, idleValue);
        cache.put(element);
    }

    /**
     * {@inheritDoc}
     * Either the element is removed from the cache
     * or it's not found in the cache and is already removed.
     * Thus the result of this op would always be true.
     */
    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            LOGGER.debug("Ticket [{}] cannot be retrieved from the cache", ticketId);
            return true;
        }

        final TicketDefinition metadata = this.ticketCatalog.find(ticket);
        final Ehcache cache = getTicketCacheFor(metadata);

        if (cache.remove(encodeTicketId(ticket.getId()))) {
            LOGGER.debug("Ticket [{}] is removed", ticket.getId());
        }
        return true;
    }

    @Override
    public long deleteAll() {
        final AtomicLong count = new AtomicLong();
        final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
        metadata.forEach(r -> {
            final Ehcache instance = getTicketCacheFor(r);
            if (instance != null) {
                count.addAndGet(instance.getSize());
                instance.removeAll();
            }
        });
        return count.get();
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        if (StringUtils.isBlank(ticketIdToGet)) {
            return null;
        }
        final TicketDefinition metadata = this.ticketCatalog.find(ticketIdToGet);
        if (metadata == null) {
            LOGGER.warn("Ticket [{}] is not registered in the catalog and is unrecognized", ticketIdToGet);
            return null;
        }

        final String ticketId = encodeTicketId(ticketIdToGet);
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }

        final Ehcache ehcache = getTicketCacheFor(metadata);
        final Element element = ehcache.get(ticketId);

        if (element == null) {
            LOGGER.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }
        final Ticket ticket = decodeTicket((Ticket) element.getObjectValue());

        final CacheConfiguration config = new CacheConfiguration();
        config.setTimeToIdleSeconds(ticket.getExpirationPolicy().getTimeToIdle());
        config.setTimeToLiveSeconds(ticket.getExpirationPolicy().getTimeToLive());

        if (element.isExpired(config) || ticket.isExpired()) {
            ehcache.evictExpiredElements();
            LOGGER.debug("Ticket [{}] has expired and is now evicted from the cache", ticket.getId());
            return null;
        }

        return ticket;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<Element> tickets = new HashSet<>();
        try {
            final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
            metadata.forEach(t -> {
                final Ehcache map = getTicketCacheFor(t);
                final Collection<Element> cacheTickets = map.getAll(map.getKeysWithExpiryCheck()).values();
                tickets.addAll(cacheTickets);
            });
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return decodeTickets(tickets.stream().map(e -> (Ticket) e.getObjectValue()).collect(Collectors.toList()));
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    private Ehcache getTicketCacheFor(final TicketDefinition metadata) {
        final String mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return this.cacheManager.getCache(mapName);
    }
}
