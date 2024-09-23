package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.IdleExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    private Ignite ignite;

    public IgniteTicketRegistry(final CipherExecutor cipherExecutor, final TicketSerializationManager ticketSerializationManager,
                                final TicketCatalog ticketCatalog, final ConfigurableApplicationContext applicationContext,
                                final IgniteConfiguration igniteConfiguration, final IgniteProperties properties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.igniteConfiguration = igniteConfiguration;
        this.properties = properties;
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val encodedTicket = encodeTicket(ticket);
        val metadata = ticketCatalog.find(ticket);
        val cache = getIgniteCacheFromMetadata(metadata);
        val policy = new IgniteInternalTicketExpiryPolicy(ticket.getExpirationPolicy());
        LOGGER.debug("Adding ticket [{}] to the cache [{}] with policy [{}]", ticket.getId(), cache.getName(), policy);
        val entries = cache.withExpiryPolicy(policy);

        val attributeMap = (Map<String, List>) collectAndDigestTicketAttributes(ticket);
        val attributesEncoded = attributeMap
            .entrySet()
            .stream()
            .map(entry -> {
                val entryValues = entry.getValue();
                val valueList = entryValues.stream().map(Object::toString).collect(Collectors.joining(","));
                return String.format("[%s:{%s}]", entry.getKey(), valueList);
            })
            .collect(Collectors.joining(","));

        val document = IgniteTicketDocument.builder()
            .id(encodedTicket.getId())
            .type(metadata.getImplementationClass().getName())
            .principal(digestIdentifier(getPrincipalIdFrom(ticket)))
            .ticket(encodedTicket)
            .prefix(metadata.getPrefix())
            .attributes(attributesEncoded)
            .build();
        entries.put(encodedTicket.getId(), document);
        return ticket;
    }

    @Override
    public long deleteAll() {
        return ticketCatalog.findAll()
            .stream()
            .map(this::getIgniteCacheFromMetadata)
            .filter(Objects::nonNull)
            .mapToLong(instance -> {
                val size = instance.size();
                instance.removeAll();
                return size;
            })
            .sum();
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketId) {
        val encTicketId = digestIdentifier(ticketId.getId());
        val metadata = ticketCatalog.find(ticketId);
        if (metadata != null) {
            val cache = getIgniteCacheFromMetadata(metadata);
            return cache.remove(encTicketId) ? 1 : 0;
        }
        return 1;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet, final Predicate<Ticket> predicate) {
        val ticketId = digestIdentifier(ticketIdToGet);
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
        val result = decodeTicket(ticket.getTicket());
        return predicate.test(result) ? result : null;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        try (val stream = stream()) {
            return stream.collect(Collectors.toSet());
        }
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        return ticketCatalog.findAll()
            .stream()
            .map(this::getIgniteCacheFromMetadata)
            .flatMap(cache -> {
                val it = cache.query(new ScanQuery<>()).spliterator();
                return StreamSupport.stream(it, false);
            })
            .skip(criteria.getFrom())
            .limit(criteria.getCount())
            .map(Cache.Entry::getValue)
            .map(IgniteTicketDocument.class::cast)
            .map(object -> decodeTicket(object.getTicket()))
            .filter(Objects::nonNull);
    }

    @Override
    public long sessionCount() {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val cacheInstance = getIgniteCacheFromMetadata(metadata);

        val queryEntity = (QueryEntity) cacheInstance.getConfiguration(CacheConfiguration.class)
            .getQueryEntities()
            .stream()
            .findFirst()
            .orElseThrow();
        val query = new SqlFieldsQuery("SELECT COUNT(id) FROM " + queryEntity.getTableName());
        return (Long) cacheInstance.query(query).getAll().getFirst().getFirst();
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val cacheInstance = getIgniteCacheFromMetadata(metadata);

        val queryEntity = (QueryEntity) cacheInstance.getConfiguration(CacheConfiguration.class)
            .getQueryEntities()
            .stream()
            .findFirst()
            .orElseThrow();
        val query = new SqlFieldsQuery("SELECT _val FROM " + queryEntity.getTableName() + " WHERE principal=?;")
            .setArgs(digestIdentifier(principalId));
        return StreamSupport.stream(cacheInstance.query(query).spliterator(), false)
            .filter(entries -> !entries.isEmpty())
            .map(entries -> (IgniteTicketDocument) entries.getFirst())
            .map(object -> decodeTicket(object.getTicket()))
            .filter(Objects::nonNull);
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val cacheInstance = getIgniteCacheFromMetadata(metadata);

        val queryEntity = (QueryEntity) cacheInstance.getConfiguration(CacheConfiguration.class)
            .getQueryEntities()
            .stream()
            .findFirst()
            .orElseThrow();

        val sql = new StringBuilder(String.format("SELECT _val FROM %s WHERE prefix='%s' AND (", queryEntity.getTableName(), metadata.getPrefix()));
        queryAttributes.forEach((key, values) ->
            values.forEach(queryValue ->
                sql.append("attributes LIKE '%[")
                    .append(digestIdentifier(key))
                    .append(":{%")
                    .append(digestIdentifier(queryValue.toString()))
                    .append("%}]%' OR ")
            ));
        sql.append("1=2);");
        LOGGER.debug("Executing SQL query [{}]", sql);
        val query = new SqlFieldsQuery(sql.toString());
        return StreamSupport.stream(cacheInstance.query(query).spliterator(), false)
            .filter(entries -> !entries.isEmpty())
            .map(entries -> (IgniteTicketDocument) entries.getFirst())
            .map(object -> decodeTicket(object.getTicket()))
            .filter(Objects::nonNull);
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

    @SuppressWarnings("UnusedVariable")
    private record IgniteInternalTicketExpiryPolicy(ExpirationPolicy expirationPolicy) implements ExpiryPolicy {
        @Override
        public Duration getExpiryForCreation() {
            return new Duration(TimeUnit.SECONDS, expirationPolicy.getTimeToLive());
        }

        @Override
        public Duration getExpiryForAccess() {
            val idleTime = expirationPolicy instanceof final IdleExpirationPolicy iep ? iep.getTimeToIdle() : expirationPolicy.getTimeToLive();
            return new Duration(TimeUnit.SECONDS, idleTime);
        }

        @Override
        public Duration getExpiryForUpdate() {
            return new Duration(TimeUnit.SECONDS, expirationPolicy.getTimeToLive());
        }
    }

    private IgniteCache<String, IgniteTicketDocument> getIgniteCacheFromMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.trace("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return getIgniteCacheInstanceByName(mapName);
    }

    private IgniteCache<String, IgniteTicketDocument> getIgniteCacheInstanceByName(final String name) {
        LOGGER.trace("Attempting to get/create cache [{}]", name);
        return ignite.getOrCreateCache(name);
    }
}
