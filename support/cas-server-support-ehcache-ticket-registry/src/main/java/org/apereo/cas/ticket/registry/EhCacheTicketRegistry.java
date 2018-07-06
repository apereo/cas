package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
@Slf4j
public class EhCacheTicketRegistry extends AbstractTicketRegistry {


    private final TicketCatalog ticketCatalog;

    private final CacheManager cacheManager;

    /**
     * Instantiates a new EhCache ticket registry.
     *
     * @param ticketCatalog the ticket catalog
     * @param cacheManager  the cache manager
     * @param cipher        the cipher
     */
    public EhCacheTicketRegistry(final TicketCatalog ticketCatalog, final CacheManager cacheManager, final CipherExecutor cipher) {
        this.ticketCatalog = ticketCatalog;
        this.cacheManager = cacheManager;
        setCipherExecutor(cipher);
        LOGGER.info("Setting up Ehcache Ticket Registry...");
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        val metadata = this.ticketCatalog.find(ticketToAdd);

        val ticket = encodeTicket(ticketToAdd);
        val element = new Element(ticket.getId(), ticket);

        var idleValue = ticketToAdd.getExpirationPolicy().getTimeToIdle().intValue();
        if (idleValue <= 0) {
            idleValue = ticketToAdd.getExpirationPolicy().getTimeToLive().intValue();
        }
        if (idleValue <= 0) {
            idleValue = Integer.MAX_VALUE;
        }
        element.setTimeToIdle(idleValue);

        var aliveValue = ticketToAdd.getExpirationPolicy().getTimeToLive().intValue();
        if (aliveValue <= 0) {
            aliveValue = Integer.MAX_VALUE;
        }
        element.setTimeToLive(aliveValue);
        val cache = getTicketCacheFor(metadata);
        LOGGER.debug("Adding ticket [{}] to the cache [{}] to live [{}] seconds and stay idle for [{}] seconds",
            ticket.getId(), cache.getName(), aliveValue, idleValue);
        cache.put(element);
    }

    /**
     * Either the element is removed from the cache
     * or it's not found in the cache and is already removed.
     * Thus the result of this op would always be true.
     */
    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val ticket = getTicket(ticketId);
        if (ticket == null) {
            LOGGER.debug("Ticket [{}] cannot be retrieved from the cache", ticketId);
            return true;
        }

        val metadata = this.ticketCatalog.find(ticket);
        val cache = getTicketCacheFor(metadata);

        if (cache.remove(encodeTicketId(ticket.getId()))) {
            LOGGER.debug("Ticket [{}] is removed", ticket.getId());
        }
        return true;
    }

    @Override
    public long deleteAll() {
        return ticketCatalog.findAll().stream()
            .map(this::getTicketCacheFor)
            .filter(Objects::nonNull)
            .mapToLong(instance -> {
                val size = instance.getSize();
                instance.removeAll();
                return size;
            }).sum();
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        if (StringUtils.isBlank(ticketIdToGet)) {
            return null;
        }
        val metadata = this.ticketCatalog.find(ticketIdToGet);
        if (metadata == null) {
            LOGGER.warn("Ticket [{}] is not registered in the catalog and is unrecognized", ticketIdToGet);
            return null;
        }

        val ticketId = encodeTicketId(ticketIdToGet);
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }

        val ehcache = getTicketCacheFor(metadata);
        val element = ehcache.get(ticketId);

        if (element == null) {
            LOGGER.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }
        val ticket = decodeTicket((Ticket) element.getObjectValue());

        val config = new CacheConfiguration();
        config.setTimeToIdleSeconds(ticket.getExpirationPolicy().getTimeToIdle());
        config.setTimeToLiveSeconds(ticket.getExpirationPolicy().getTimeToLive());

        if (element.isExpired(config) || ticket.isExpired()) {
            ehcache.remove(element);
            LOGGER.debug("Ticket [{}] has expired and is now evicted from the cache", ticket.getId());
            return null;
        }

        return ticket;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCacheFor)
            .flatMap(map -> getAllExpired(map).values().stream())
            .map(e -> (Ticket) e.getObjectValue())
            .map(this::decodeTicket)
            .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    private Ehcache getTicketCacheFor(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return this.cacheManager.getCache(mapName);
    }

    private Map<Object, Element> getAllExpired(final Ehcache map) {
        try {
            return map.getAll(map.getKeysWithExpiryCheck());
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return new HashMap<>(0);
        }
    }
}
