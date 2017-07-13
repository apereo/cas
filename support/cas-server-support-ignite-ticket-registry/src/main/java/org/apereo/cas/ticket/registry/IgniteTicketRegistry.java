package org.apereo.cas.ticket.registry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.ssl.SslContextFactory;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
 * @since 5.0.0`
 */
public class IgniteTicketRegistry extends AbstractTicketRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteTicketRegistry.class);

    private final IgniteConfiguration igniteConfiguration;
    private final IgniteProperties properties;

    private final TicketCatalog ticketCatalog;
    private Ignite ignite;

    /**
     * Instantiates a new Ignite ticket registry.
     *
     * @param ticketCatalog       the ticket catalog
     * @param igniteConfiguration the ignite configuration
     * @param properties          the properties
     */
    public IgniteTicketRegistry(final TicketCatalog ticketCatalog,
                                final IgniteConfiguration igniteConfiguration, final IgniteProperties properties) {
        this.igniteConfiguration = igniteConfiguration;
        this.properties = properties;
        this.ticketCatalog = ticketCatalog;

        initializeIgnite();
    }

    @Override
    public void addTicket(final Ticket ticket) {
        final Ticket encodedTicket = encodeTicket(ticket);

        final TicketDefinition metadata = this.ticketCatalog.find(ticket);
        final IgniteCache<String, Ticket> cache = getIgniteCacheFromMetadata(metadata);
        LOGGER.debug("Adding ticket [{}] to the cache [{}]", ticket.getId(), cache.getName());
        cache.withExpiryPolicy(new IgniteInternalTicketExpiryPolicy(ticket)).put(encodedTicket.getId(), encodedTicket);
    }

    @Override
    public long deleteAll() {
        return this.ticketCatalog.findAll().stream()
                .map(this::getIgniteCacheFromMetadata)
                .filter(Objects::nonNull)
                .mapToLong(instance -> {
                    final int size = instance.size();
                    instance.removeAll();
                    return size;
                })
                .sum();
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        if (ticket != null) {
            final TicketDefinition metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.warn("Ticket [{}] is not registered in the catalog and is unrecognized", ticketId);
                return false;
            }
            final IgniteCache<String, Ticket> cache = getIgniteCacheFromMetadata(metadata);
            return cache.remove(encodeTicketId(ticket.getId()));
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        final String ticketId = encodeTicketId(ticketIdToGet);
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }

        final TicketDefinition metadata = this.ticketCatalog.find(ticketIdToGet);
        if (metadata == null) {
            LOGGER.warn("Ticket [{}] is not registered in the catalog and is unrecognized", ticketIdToGet);
            return null;
        }
        final IgniteCache<String, Ticket> cache = getIgniteCacheFromMetadata(metadata);
        final Ticket ticket = cache.get(ticketId);
        if (ticket == null) {
            LOGGER.debug("No ticket by id [{}] is found in the ignite ticket registry", ticketId);
            return null;
        }
        return decodeTicket(ticket);
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
                .map(this::getIgniteCacheFromMetadata)
                .map(cache -> cache.query(new ScanQuery<>()).getAll().stream())
                .flatMap(Function.identity())
                .map(Cache.Entry::getValue)
                .map(object -> decodeTicket((Ticket) object))
                .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    private void configureSecureTransport() {
        final String nullKey = "NULL";

        if (StringUtils.isNotBlank(properties.getKeyStoreFilePath())
                && StringUtils.isNotBlank(properties.getKeyStorePassword())
                && StringUtils.isNotBlank(properties.getTrustStoreFilePath())
                && StringUtils.isNotBlank(properties.getTrustStorePassword())) {

            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStoreFilePath(properties.getKeyStoreFilePath());
            sslContextFactory.setKeyStorePassword(properties.getKeyStorePassword().toCharArray());

            if (nullKey.equals(properties.getTrustStoreFilePath()) && nullKey.equals(properties.getTrustStorePassword())) {
                sslContextFactory.setTrustManagers(SslContextFactory.getDisabledTrustManager());
            } else {
                sslContextFactory.setTrustStoreFilePath(properties.getTrustStoreFilePath());
                sslContextFactory.setTrustStorePassword(properties.getKeyStorePassword().toCharArray());
            }

            if (StringUtils.isNotBlank(properties.getKeyAlgorithm())) {
                sslContextFactory.setKeyAlgorithm(properties.getKeyAlgorithm());
            }
            if (StringUtils.isNotBlank(properties.getProtocol())) {
                sslContextFactory.setProtocol(properties.getProtocol());
            }
            if (StringUtils.isNotBlank(properties.getTrustStoreType())) {
                sslContextFactory.setTrustStoreType(properties.getTrustStoreType());
            }
            if (StringUtils.isNotBlank(properties.getKeyStoreType())) {
                sslContextFactory.setKeyStoreType(properties.getKeyStoreType());
            }
            this.igniteConfiguration.setSslContextFactory(sslContextFactory);
        }
    }

    private void initializeIgnite() {
        LOGGER.info("Setting up Ignite Ticket Registry...");

        configureSecureTransport();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("igniteConfiguration.cacheConfiguration=[{}]", (Object[]) this.igniteConfiguration.getCacheConfiguration());
            LOGGER.debug("igniteConfiguration.getDiscoverySpi=[{}]", this.igniteConfiguration.getDiscoverySpi());
            LOGGER.debug("igniteConfiguration.getSslContextFactory=[{}]", this.igniteConfiguration.getSslContextFactory());
        }

        if (Ignition.state() == IgniteState.STOPPED) {
            this.ignite = Ignition.start(this.igniteConfiguration);
            LOGGER.debug("Starting ignite cache engine");
        } else if (Ignition.state() == IgniteState.STARTED) {
            this.ignite = Ignition.ignite();
            LOGGER.debug("Ignite cache engine has started");
        }
    }

    /**
     * Make sure we shutdown Ignite when the context is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        Ignition.stopAll(true);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("igniteConfiguration", properties)
                .toString();
    }

    private IgniteCache<String, Ticket> getIgniteCacheFromMetadata(final TicketDefinition metadata) {
        final String mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return getIgniteCacheInstanceByName(mapName);
    }

    private IgniteCache<String, Ticket> getIgniteCacheInstanceByName(final String name) {
        LOGGER.debug("Attempting to get/create cache [{}]", name);
        return this.ignite.getOrCreateCache(name);
    }

    private static class IgniteInternalTicketExpiryPolicy implements ExpiryPolicy {
        private final Ticket ticket;

        /**
         * Instantiates a new Ignite internal ticket expiry policy.
         *
         * @param ticket the ticket
         */
        IgniteInternalTicketExpiryPolicy(final Ticket ticket) {
            this.ticket = ticket;
        }

        @Override
        public Duration getExpiryForCreation() {
            return new Duration(TimeUnit.SECONDS, ticket.getExpirationPolicy().getTimeToLive());
        }

        @Override
        public Duration getExpiryForAccess() {
            final long idleTime = ticket.getExpirationPolicy().getTimeToIdle() <= 0
                    ? ticket.getExpirationPolicy().getTimeToLive()
                    : ticket.getExpirationPolicy().getTimeToIdle();
            return new Duration(TimeUnit.SECONDS, idleTime);
        }

        @Override
        public Duration getExpiryForUpdate() {
            return new Duration(TimeUnit.SECONDS, ticket.getExpirationPolicy().getTimeToLive());
        }
    }
}
