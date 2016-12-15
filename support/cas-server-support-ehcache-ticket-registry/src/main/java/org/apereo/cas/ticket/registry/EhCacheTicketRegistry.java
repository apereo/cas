package org.apereo.cas.ticket.registry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.ticket.Ticket;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Collection;
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
    
    private Cache ehcacheTicketsCache;

    private boolean supportRegistryState = true;

    /**
     * Instantiates a new EhCache ticket registry.
     */
    public EhCacheTicketRegistry() {
    }

    /**
     * Instantiates a new EhCache ticket registry.
     *
     * @param ticketCache the ticket cache
     */
    public EhCacheTicketRegistry(final Cache ticketCache) {
        setEhcacheTicketsCache(ticketCache);
    }

    /**
     * Instantiates a new EhCache ticket registry.
     *
     * @param ticketCache          the ticket cache
     * @param supportRegistryState the support registry state
     */
    public EhCacheTicketRegistry(final Cache ticketCache,
            final boolean supportRegistryState) {
        this(ticketCache);
        setSupportRegistryState(supportRegistryState);
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);
        final Element element = new Element(ticket.getId(), ticket);
        
        int idleValue = ticketToAdd.getExpirationPolicy().getTimeToIdle().intValue();
        if (idleValue <= 0) {
            idleValue = ticketToAdd.getExpirationPolicy().getTimeToLive().intValue();
        }
        element.setTimeToIdle(idleValue);
        final int aliveValue = ticketToAdd.getExpirationPolicy().getTimeToLive().intValue();
        element.setTimeToLive(aliveValue);

        logger.debug("Adding ticket {} to the cache {} to live {} seconds and stay idle for {} seconds",
                ticket.getId(), this.ehcacheTicketsCache.getName(), aliveValue, idleValue);
        this.ehcacheTicketsCache.put(element);
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
            logger.debug("Ticket {} cannot be retrieved from the cache", ticketId);
            return true;
        }

        if (this.ehcacheTicketsCache.remove(ticket.getId())) {
            logger.debug("Ticket {} is removed", ticket.getId());
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        final String ticketId = encodeTicketId(ticketIdToGet);
        if (ticketId == null) {
            return null;
        }

        final Element element = this.ehcacheTicketsCache.get(ticketId);
        if (element == null) {
            logger.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }
        final Ticket ticket = decodeTicket((Ticket) element.getObjectValue());

        final CacheConfiguration config = new CacheConfiguration();
        config.setTimeToIdleSeconds(ticket.getExpirationPolicy().getTimeToIdle());
        config.setTimeToLiveSeconds(ticket.getExpirationPolicy().getTimeToLive());
        
        if (element.isExpired(config) || ticket.isExpired()) {
            logger.debug("Ticket {} has expired", ticket.getId());
            this.ehcacheTicketsCache.evictExpiredElements();
            this.ehcacheTicketsCache.flush();
            return null;
        }
        
        return ticket;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<Element> cacheTickets =
                this.ehcacheTicketsCache.getAll(this.ehcacheTicketsCache.getKeysWithExpiryCheck()).values();
        return decodeTickets(cacheTickets.stream().map(e -> (Ticket) e.getObjectValue()).collect(Collectors.toList()));
    }


    @Override
    public void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }
    
    /**
     * Flag to indicate whether this registry instance should participate in reporting its state with
     * default value set to {@code true}.
     * Based on the <a href="http://ehcache.org/apidocs/net/sf/ehcache/Ehcache.html#getKeysWithExpiryCheck()">EhCache documentation</a>,
     * determining the number of service tickets and the total session count from the cache can be considered
     * an expensive operation with the time taken as O(n), where n is the number of elements in the cache.
     *
     * <p>Therefore, the flag provides a level of flexibility such that depending on the cache and environment
     * settings, reporting statistics
     * can be set to false and disabled.</p>
     *
     * @param supportRegistryState true, if the registry is to support registry state
     * @see #sessionCount()
     * @see #serviceTicketCount()
     */
    public void setSupportRegistryState(final boolean supportRegistryState) {
        this.supportRegistryState = supportRegistryState;
    }
    
    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up Ehcache Ticket Registry...");

        Assert.notNull(this.ehcacheTicketsCache, "Ehcache Tickets cache cannot nbe null");
        if (logger.isDebugEnabled()) {
            final CacheConfiguration config = this.ehcacheTicketsCache.getCacheConfiguration();
            logger.debug("TicketCache.maxEntriesLocalHeap={}", config.getMaxEntriesLocalHeap());
            logger.debug("TicketCache.maxEntriesLocalDisk={}", config.getMaxEntriesLocalDisk());
            logger.debug("TicketCache.maxEntriesInCache={}", config.getMaxEntriesInCache());
            logger.debug("TicketCache.persistenceConfiguration={}", config.getPersistenceConfiguration().getStrategy());
            logger.debug("TicketCache.synchronousWrites={}", config.getPersistenceConfiguration().getSynchronousWrites());
            logger.debug("TicketCache.timeToLive={}", config.getTimeToLiveSeconds());
            logger.debug("TicketCache.timeToIdle={}", config.getTimeToIdleSeconds());
            logger.debug("TicketCache.cacheManager={}", this.ehcacheTicketsCache.getCacheManager().getName());
        }
    }

    public void setEhcacheTicketsCache(final Cache ehcacheTicketsCache) {
        this.ehcacheTicketsCache = ehcacheTicketsCache;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("ehcacheTicketsCache", this.ehcacheTicketsCache)
                .append("supportRegistryState", this.supportRegistryState)
                .toString();
    }
    
    
}
