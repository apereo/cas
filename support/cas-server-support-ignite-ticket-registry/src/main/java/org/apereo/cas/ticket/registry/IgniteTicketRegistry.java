package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.beans.factory.DisposableBean;

import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * <a href="https://ignite.apache.org">Ignite</a> based distributed ticket registry.
 * </p>
 * <p>
 * Use distinct caches for ticket granting tickets (TGT) and service tickets (ST) for:
 * </p>
 * <ul>
 * <li>Tuning: use cache level time to live with different values for TGT an ST.</li>
 * <li>Monitoring: follow separately the number of TGT and ST.</li>
 * </ul>
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
@Slf4j
@ToString(callSuper = true)
public class IgniteTicketRegistry extends AbstractTicketRegistry implements DisposableBean {

    private final IgniteConfiguration igniteConfiguration;

    private final IgniteProperties properties;

    private final TicketCatalog ticketCatalog;

    private Ignite ignite;

    public IgniteTicketRegistry(final TicketCatalog ticketCatalog, final IgniteConfiguration igniteConfiguration, final IgniteProperties properties) {
        this.igniteConfiguration = igniteConfiguration;
        this.properties = properties;
        this.ticketCatalog = ticketCatalog;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        val encodedTicket = encodeTicket(ticket);
        val metadata = this.ticketCatalog.find(ticket);
        val cache = getIgniteCacheFromMetadata(metadata);
        val policy = new IgniteInternalTicketExpiryPolicy(ticket.getExpirationPolicy());
        LOGGER.debug("Adding ticket [{}] to the cache [{}] with policy [{}]", ticket.getId(), cache.getName(), policy);
        val entries = cache.withExpiryPolicy(policy);
        entries.put(encodedTicket.getId(), encodedTicket);
    }

    @Override
    public long deleteAll() {
        return this.ticketCatalog.findAll().stream().map(this::getIgniteCacheFromMetadata).filter(Objects::nonNull).mapToLong(instance -> {
            val size = instance.size();
            instance.removeAll();
            return size;
        }).sum();
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val ticket = getTicket(ticketId);
        if (ticket != null) {
            val metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.warn("Ticket [{}] is not registered in the catalog and is unrecognized", ticketId);
                return false;
            }
            val cache = getIgniteCacheFromMetadata(metadata);
            return cache.remove(encodeTicketId(ticket.getId()));
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet, final Predicate<Ticket> predicate) {
        val ticketId = encodeTicketId(ticketIdToGet);
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }
        LOGGER.debug("Encoded ticket id is [{}]", ticketId);
        val metadata = this.ticketCatalog.find(ticketIdToGet);
        if (metadata == null) {
            LOGGER.warn("Ticket [{}] is not registered in the catalog and is unrecognized", ticketIdToGet);
            return null;
        }
        val cache = getIgniteCacheFromMetadata(metadata);
        LOGGER.trace("Located ignite cache [{}] for ticket id [{}]", cache.getName(), ticketId);
        val ticket = cache.get(ticketId);
        LOGGER.trace("Located ticket from cache for ticket id [{}] is [{}]", ticketId, ticket);
        if (ticket == null) {
            LOGGER.debug("No ticket by id [{}] is found in the ignite ticket registry", ticketId);
            return null;
        }
        val result = decodeTicket(ticket);
        if (predicate.test(result)) {
            return result;
        }
        LOGGER.debug("Unable to decode ticket [{}]", ticket);
        return null;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream().map(this::getIgniteCacheFromMetadata)
            .map(cache -> cache.query(new ScanQuery<>()).getAll().stream()).flatMap(Function.identity())
            .map(Cache.Entry::getValue).map(object -> decodeTicket((Ticket) object)).collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    /**
     * Make sure we shutdown Ignite when the context is destroyed.
     */
    public void shutdown() {
        this.ignite.close();
        Ignition.stopAll(true);
    }

    @Override
    public void destroy() {
        shutdown();
    }

    /**
     * Initialize.
     */
    public void initialize() {
        if (Ignition.state() == IgniteState.STOPPED) {
            this.ignite = Ignition.start(igniteConfiguration);
            LOGGER.debug("Starting ignite cache engine");
        } else if (Ignition.state() == IgniteState.STARTED) {
            this.ignite = Ignition.ignite();
            LOGGER.debug("Ignite cache engine has started");
        }
    }

    private IgniteCache<String, Ticket> getIgniteCacheFromMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.trace("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return getIgniteCacheInstanceByName(mapName);
    }

    private IgniteCache<String, Ticket> getIgniteCacheInstanceByName(final String name) {
        LOGGER.trace("Attempting to get/create cache [{}]", name);
        return this.ignite.getOrCreateCache(name);
    }

    @ToString
    private static class IgniteInternalTicketExpiryPolicy implements ExpiryPolicy {

        private final ExpirationPolicy expirationPolicy;

        IgniteInternalTicketExpiryPolicy(final ExpirationPolicy ticket) {
            this.expirationPolicy = ticket;
        }

        @Override
        public Duration getExpiryForCreation() {
            return new Duration(TimeUnit.SECONDS, expirationPolicy.getTimeToLive());
        }

        @Override
        public Duration getExpiryForAccess() {
            val idleTime = expirationPolicy.getTimeToIdle() <= 0
                ? expirationPolicy.getTimeToLive() : expirationPolicy.getTimeToIdle();
            return new Duration(TimeUnit.SECONDS, idleTime);
        }

        @Override
        public Duration getExpiryForUpdate() {
            return new Duration(TimeUnit.SECONDS, expirationPolicy.getTimeToLive());
        }
    }
}
