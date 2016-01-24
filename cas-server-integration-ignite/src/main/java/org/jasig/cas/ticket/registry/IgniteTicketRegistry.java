package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.registry.encrypt.AbstractCrypticTicketRegistry;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.cache.Cache;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * <a href="https://ignite.apache.org">Ignite</a> based distributed ticket registry.
 * </p>
 *
 * <p>
 * Use distinct caches for ticket granting tickets (TGT) and service tickets (ST) for:
 * <ul>
 *   <li>Tuning : use cache level time to live with different values for TGT an ST.</li>
 *   <li>Monitoring : follow separately the number of TGT and ST.</li>
 * </ul>
 *
 * @author tduehr
 * @since 4.3.0`
 */
@Component("igniteTicketRegistry")
public final class IgniteTicketRegistry extends AbstractCrypticTicketRegistry {

    @Autowired
    @NotNull
    @Value("${ignite.ticketsCache.name:serviceTicketsCache}")
    private String servicesCacheName;

    @NotNull
    @Value("${ignite.ticketsCache.name:ticketGrantingTicketsCache}")
    private String ticketsCacheName;

    @Value("${ignite.keyStoreType:}")
    private String keyStoreType;

    @Value("${ignite.keyStoreFilePath:}")
    private String keyStoreFilePath;

    @Value("${ignite.keyStorePassword:}")
    private String keyStorePassword;

    @Value("${ignite.trustStoreType:}")
    private String trustStoreType;

    @Value("${ignite.protocol:}")
    private String protocol;

    @Value("${ignite.keyAlgorithm:}")
    private String keyAlgorithm;

    @Value("${ignite.trustStoreFilePath:}")
    private String trustStoreFilePath;

    @Value("${ignite.trustStorePassword:}")
    private String trustStorePassword;

    @Autowired
    @NotNull
    @Qualifier("igniteConfiguration")
    private IgniteConfiguration igniteConfiguration;

    private IgniteCache<String, ServiceTicket> serviceTicketsCache;
    private IgniteCache<String, TicketGrantingTicket> ticketGrantingTicketsCache;

    @Value("${tgt.maxTimeToLiveInSeconds:28800}")
    private long ticketGrantingTicketTimeoutInSeconds = 28800;

    @Value("${st.timeToKillInSeconds:10}")
    private long serviceTicketTimeoutInSeconds = 10;

    private Ignite ignite;

    /**
     * @see #setSupportRegistryState(boolean)
     **/
    private boolean supportRegistryState = true;

    /**
     * Instantiates a new Ignite ticket registry.
     */
    public IgniteTicketRegistry() {
        super();
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);
        if (ticket instanceof ServiceTicket) {
            logger.debug("Adding service ticket {} to the cache {}", ticket.getId(), this.serviceTicketsCache.getName());
            this.serviceTicketsCache.put(ticket.getId(), (ServiceTicket) ticket);
        } else if (ticket instanceof TicketGrantingTicket) {
            logger.debug("Adding ticket granting ticket {} to the cache {}", ticket.getId(), this.ticketGrantingTicketsCache.getName());
            this.ticketGrantingTicketsCache.put(ticket.getId(), (TicketGrantingTicket) ticket);
        } else {
            throw new IllegalArgumentException("Invalid ticket type " + ticket);
        }
    }

    @Override
    public boolean deleteTicket(final String ticketIdToDelete) {
        final String ticketId = encodeTicketId(ticketIdToDelete);
        if (StringUtils.isBlank(ticketId)) {
            return false;
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return false;
        }

        if (ticket instanceof TicketGrantingTicket) {
            logger.debug("Removing ticket [{}] and its children from the registry.", ticket);
            return deleteTicketAndChildren((TicketGrantingTicket) ticket);
        }

        logger.debug("Removing ticket [{}] from the registry.", ticket);
        return this.serviceTicketsCache.remove(ticketId);
    }

    /**
     * Delete the TGT and all of its service tickets.
     *
     * @param ticket the ticket
     * @return boolean indicating whether ticket was deleted or not
     */
    private boolean deleteTicketAndChildren(final TicketGrantingTicket ticket) {
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            this.serviceTicketsCache.removeAll(services.keySet());
        }

        return this.ticketGrantingTicketsCache.remove(ticket.getId());
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        final String ticketId = encodeTicketId(ticketIdToGet);
        if (ticketId == null) {
            return null;
        }

        Ticket ticket = this.serviceTicketsCache.get(ticketId);
        if (ticket == null) {
            ticket = this.ticketGrantingTicketsCache.get(ticketId);
        }
        if (ticket == null) {
            logger.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }

        final Ticket proxiedTicket = decodeTicket(ticket);
        ticket = getProxiedTicketInstance(proxiedTicket);
        return ticket;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<Cache.Entry<String, Ticket>> serviceTickets;
        final Collection<Cache.Entry<String, Ticket>> tgtTicketsTickets;

        final IgniteBiPredicate<String, Ticket> filter = new IgniteBiPredicate<String, Ticket>() {
          @Override
          public boolean apply(final String key, final Ticket t) {
            return !t.isExpired();
          }
        };

        QueryCursor<Cache.Entry<String, Ticket>> cursor = ticketGrantingTicketsCache.query(new ScanQuery<>(filter));
        tgtTicketsTickets = cursor.getAll();

        cursor = serviceTicketsCache.query(new ScanQuery<>(filter));
        serviceTickets = cursor.getAll();

        final Collection<Ticket> allTickets = new HashSet<>(serviceTickets.size() + tgtTicketsTickets.size());

        for (final Cache.Entry<String, Ticket> entry : serviceTickets) {
            final Ticket proxiedTicket = getProxiedTicketInstance(entry.getValue());
            allTickets.add(proxiedTicket);
        }

        for (final Cache.Entry<String, Ticket> entry : tgtTicketsTickets) {
            final Ticket proxiedTicket = getProxiedTicketInstance(entry.getValue());
            allTickets.add(proxiedTicket);
        }

        return decodeTickets(allTickets);
    }

    public void setServiceTicketsCache(final IgniteCache<String, ServiceTicket> serviceTicketsCache) {
        this.serviceTicketsCache = serviceTicketsCache;
    }

    public void setTicketGrantingTicketsCache(final IgniteCache<String, TicketGrantingTicket> ticketGrantingTicketsCache) {
        this.ticketGrantingTicketsCache = ticketGrantingTicketsCache;
    }

    public void setIgniteConfiguration(final IgniteConfiguration igniteConfiguration){
        this.igniteConfiguration = igniteConfiguration;
    }

    public IgniteConfiguration getIgniteConfiguration(){
        return this.igniteConfiguration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ticketGrantingTicketsCache", this.ticketGrantingTicketsCache)
                .append("serviceTicketsCache", this.serviceTicketsCache).toString();
    }

    @Override
    protected void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }

    /**
     * Flag to indicate whether this registry instance should participate in reporting its state with
     * default value set to {@code true}.
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

    private void configureSecureTransport() {
        if (StringUtils.isNotBlank(this.keyStoreFilePath) && StringUtils.isNotBlank(this.keyStorePassword)
            && StringUtils.isNotBlank(this.trustStoreFilePath) && StringUtils.isNotBlank(this.trustStorePassword)) {
            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStoreFilePath(this.keyStoreFilePath);
            sslContextFactory.setKeyStorePassword(this.keyStorePassword.toCharArray());
            if ("NULL".equals(this.trustStoreFilePath) && "NULL".equals(this.trustStorePassword)){
                sslContextFactory.setTrustManagers(SslContextFactory.getDisabledTrustManager());
            } else {
                sslContextFactory.setTrustStoreFilePath(this.trustStoreFilePath);
                sslContextFactory.setTrustStorePassword(this.trustStorePassword.toCharArray());
            }

            if (StringUtils.isNotBlank(this.keyAlgorithm)){
                sslContextFactory.setKeyAlgorithm(this.keyAlgorithm);
            }
            if (StringUtils.isNotBlank(this.protocol)){
                sslContextFactory.setProtocol(this.protocol);
            }
            if (StringUtils.isNotBlank(this.trustStoreType)){
                sslContextFactory.setTrustStoreType(this.trustStoreType);
            }
            if (StringUtils.isNotBlank(this.keyStoreType)){
                sslContextFactory.setKeyStoreType(this.keyStoreType);
            }
            this.igniteConfiguration.setSslContextFactory(sslContextFactory);
        }
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up Ignite Ticket Registry...");

        configureSecureTransport();

        if (logger.isDebugEnabled()) {
            logger.debug("igniteConfiguration.cacheConfiguration={}", igniteConfiguration.getCacheConfiguration());
            logger.debug("igniteConfiguration.getDiscoverySpi={}", igniteConfiguration.getDiscoverySpi());
            logger.debug("igniteConfiguration.getSslContextFactory={}", igniteConfiguration.getSslContextFactory());
            logger.debug("Ticket-granting ticket timeout: [{}s]", this.ticketGrantingTicketTimeoutInSeconds);
            logger.debug("Service ticket timeout: [{}s]", this.serviceTicketTimeoutInSeconds);
        }

        if (Ignition.state() == IgniteState.STOPPED) {
            ignite = Ignition.start(igniteConfiguration);
        } else if (Ignition.state() == IgniteState.STARTED) {
            ignite = Ignition.ignite();
        }

        serviceTicketsCache = ignite.getOrCreateCache(servicesCacheName);
        serviceTicketsCache.getConfiguration(CacheConfiguration.class)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, serviceTicketTimeoutInSeconds)));

        ticketGrantingTicketsCache = ignite.getOrCreateCache(ticketsCacheName);
        ticketGrantingTicketsCache.getConfiguration(CacheConfiguration.class)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, ticketGrantingTicketTimeoutInSeconds)));
    }

    @Override
    public int sessionCount() {
        return BooleanUtils.toInteger(this.supportRegistryState, this.ticketGrantingTicketsCache
            .size(CachePeekMode.ALL), super.sessionCount());
    }

    @Override
    public int serviceTicketCount() {
        return BooleanUtils.toInteger(this.supportRegistryState, this.serviceTicketsCache
            .size(CachePeekMode.ALL), super.serviceTicketCount());
    }

    /**
     * Make sure we shutdown Ignite when the context is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        Ignition.stopAll(true);
    }

    public String getTicketsCacheName() {
        return ticketsCacheName;
    }

    public void setTicketsCacheName(final String cacheName) {
        this.ticketsCacheName = cacheName;
    }

    public String getServicesCacheName() {
        return servicesCacheName;
    }

    public void setServicesCacheName(final String cacheName) {
        this.servicesCacheName = cacheName;
    }
}
