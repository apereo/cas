package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;

import javax.cache.Cache;
import javax.cache.CacheManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * <a href="http://ehcache.org/">Ehcache 3</a> based distributed ticket registry.
 * </p>
 *
 * @author Hal Deadman
 * @since 6.2
 */
@Slf4j
public class EhCache3TicketRegistry extends AbstractTicketRegistry implements DisposableBean {


    private final TicketCatalog ticketCatalog;

    private final CacheManager cacheManager;

    /**
     * Instantiates a new EhCache ticket registry.
     *
     * @param ticketCatalog the ticket catalog
     * @param cacheManager  the cache manager
     * @param cipher        the cipher
     */
    public EhCache3TicketRegistry(final TicketCatalog ticketCatalog,
                                  final CacheManager cacheManager,
                                  final CipherExecutor cipher) {
        this.ticketCatalog = ticketCatalog;
        this.cacheManager = cacheManager;
        setCipherExecutor(cipher);
        LOGGER.info("Setting up Ehcache Ticket Registry...");
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        val metadata = this.ticketCatalog.find(ticketToAdd);

        val ticket = encodeTicket(ticketToAdd);

        val cache = getTicketCacheFor(metadata);
        LOGGER.debug("Adding ticket [{}] to the cache: {}",
            ticket.getId(), metadata.getProperties().getStorageName());
        cache.put(ticket.getId(), ticket);
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val ticket = getTicket(ticketId, p -> true);
        if (ticket == null) {
            LOGGER.debug("Ticket [{}] cannot be retrieved from the cache", ticketId);
            return true;
        }

        val metadata = this.ticketCatalog.find(ticket);
        val cache = getTicketCacheFor(metadata);

        val encodedTicketKey = encodeTicketId(ticket.getId());
        if (cache.containsKey(encodedTicketKey)) {
            cache.remove(encodedTicketKey);
            LOGGER.debug("Ticket [{}] is removed", ticket.getId());
        }
        return true;
    }

    @Override
    public long deleteAll() {
        ticketCatalog.findAll().stream()
            .map(this::getTicketCacheFor)
            .filter(Objects::nonNull)
            .forEach(Cache::clear);
        return -1;
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
        val encodedTicket = ehcache.get(ticketId);

        if (encodedTicket == null) {
            LOGGER.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }

        val ticket = decodeTicket(encodedTicket);

        if (predicate.test(ticket)) {
            return ticket;
        }
        return null;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCacheFor)
            .flatMap(map -> getAllUnexpired(map).values().stream())
            .map(this::decodeTicket)
            .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    private Cache<String, Ticket> getTicketCacheFor(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return this.cacheManager.getCache(mapName, String.class, Ticket.class);
    }

    private static Map<String, Ticket> getAllUnexpired(final Cache<String, Ticket> map) {
        try {
            val returnMap = new HashMap<String, Ticket>();
            map.iterator().forEachRemaining(entry -> returnMap.put(entry.getKey(), entry.getValue()));
            return returnMap;
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(e.getMessage(), e);
            } else {
                LOGGER.warn(e.getMessage());
            }
            return new HashMap<>(0);
        }
    }

    @Override
    public void destroy() {
        if (!this.cacheManager.isClosed()) {
            this.cacheManager.close();
        }
    }
}
