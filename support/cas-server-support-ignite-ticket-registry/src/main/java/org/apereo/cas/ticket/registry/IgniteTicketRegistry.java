package org.apereo.cas.ticket.registry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.ssl.SslContextFactory;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

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

    private IgniteCache<String, Ticket> ticketIgniteCache;

    private Ignite ignite;
    
    private boolean supportRegistryState = true;

    /**
     * Instantiates a new Ignite ticket registry.
     *
     * @param igniteConfiguration the ignite configuration
     * @param properties          the properties
     */
    public IgniteTicketRegistry(final IgniteConfiguration igniteConfiguration, final IgniteProperties properties) {
        this.igniteConfiguration = igniteConfiguration;
        this.properties = properties;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);
        LOGGER.debug("Adding ticket [{}] to the cache [{}]", ticket.getId(), this.ticketIgniteCache.getName());
        this.ticketIgniteCache.withExpiryPolicy(new ExpiryPolicy() {
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
        }).put(ticket.getId(), ticket);
    }

    @Override
    public long deleteAll() {
        final int size = this.ticketIgniteCache.size();
        this.ticketIgniteCache.removeAll();
        return size;
    }
    
    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        if (ticket != null) {
            return this.ticketIgniteCache.remove(ticket.getId());
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        final String ticketId = encodeTicketId(ticketIdToGet);
        if (ticketId == null) {
            return null;
        }

        final Ticket ticket = this.ticketIgniteCache.get(ticketId);
        if (ticket == null) {
            LOGGER.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }
        return decodeTicket(ticket);
    }

    @Override
    public Collection<Ticket> getTickets() {
        final QueryCursor<Cache.Entry<String, Ticket>> cursor = this.ticketIgniteCache.query(new ScanQuery<>((key, t) -> !t.isExpired()));
        return decodeTickets(cursor.getAll().stream().map(Cache.Entry::getValue).collect(toList()));
    }

    public void setTicketIgniteCache(final IgniteCache<String, Ticket> ticketIgniteCache) {
        this.ticketIgniteCache = ticketIgniteCache;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    /**
     * Flag to indicate whether this registry instance should participate in reporting its state with
     * default value set to {@code true}.
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

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        LOGGER.info("Setting up Ignite Ticket Registry...");

        configureSecureTransport();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("igniteConfiguration.cacheConfiguration=[{}]", (Object[]) this.igniteConfiguration.getCacheConfiguration());
            LOGGER.debug("igniteConfiguration.getDiscoverySpi=[{}]", this.igniteConfiguration.getDiscoverySpi());
            LOGGER.debug("igniteConfiguration.getSslContextFactory=[{}]", this.igniteConfiguration.getSslContextFactory());
        }

        if (Ignition.state() == IgniteState.STOPPED) {
            this.ignite = Ignition.start(this.igniteConfiguration);
        } else if (Ignition.state() == IgniteState.STARTED) {
            this.ignite = Ignition.ignite();
        }

        this.ticketIgniteCache = this.ignite.getOrCreateCache(properties.getTicketsCache().getCacheName());
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
                .append("supportRegistryState", this.supportRegistryState)
                .toString();
    }
}
