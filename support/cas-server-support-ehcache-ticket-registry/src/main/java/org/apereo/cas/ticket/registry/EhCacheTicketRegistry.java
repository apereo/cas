package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.util.crypto.CipherExecutor;

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
import java.util.function.Predicate;
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
 * @deprecated Since 6.2, due to Ehcache 2.x being unmaintained. Other registries are available, including Ehcache 3.x.
 */
@Slf4j
@Deprecated(since = "6.2.0")
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

    private static Map<Object, Element> getAllUnexpired(final Ehcache map) {
        try {
            return map.getAll(map.getKeysWithExpiryCheck());
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return new HashMap<>(0);
        }
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        val metadata = this.ticketCatalog.find(ticketToAdd);

        val ticket = encodeTicket(ticketToAdd);
        val element = new Element(ticket.getId(), ticket);

        val expirationPolicy = ticketToAdd.getExpirationPolicy();
        var idleValue = expirationPolicy.getTimeToIdle().intValue();
        if (idleValue <= 0) {
            idleValue = expirationPolicy.getTimeToLive().intValue();
        }
        if (idleValue <= 0) {
            idleValue = Integer.MAX_VALUE;
        }
        element.setTimeToIdle(idleValue);

        var aliveValue = expirationPolicy.getTimeToLive().intValue();
        if (aliveValue <= 0) {
            aliveValue = Integer.MAX_VALUE;
        }
        element.setTimeToLive(aliveValue);
        val cache = getTicketCacheFor(metadata);
        LOGGER.debug("Adding ticket [{}] to the cache [{}] to live [{}] seconds and stay idle for [{}] seconds",
            ticket.getId(), cache.getName(), aliveValue, idleValue);
        cache.put(element);
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet, final Predicate<Ticket> predicate) {
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
        val expirationPolicy = ticket.getExpirationPolicy();
        config.setTimeToIdleSeconds(expirationPolicy.getTimeToIdle());
        config.setTimeToLiveSeconds(expirationPolicy.getTimeToLive());

        if (!element.isExpired(config) && predicate.test(ticket)) {
            return ticket;
        }
        return null;
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
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCacheFor)
            .flatMap(map -> getAllUnexpired(map).values().stream())
            .map(e -> (Ticket) e.getObjectValue())
            .map(this::decodeTicket)
            .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val ticket = getTicket(ticketId, ticket1 -> true);
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

    private Ehcache getTicketCacheFor(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return this.cacheManager.getCache(mapName);
    }
}
