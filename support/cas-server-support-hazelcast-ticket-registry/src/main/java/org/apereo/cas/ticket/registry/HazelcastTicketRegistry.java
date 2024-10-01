package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastTicketRegistryProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
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
 * Hazelcast-based implementation of a {@link TicketRegistry}.
 * <p>This implementation just wraps the Hazelcast's {@link IMap}
 * which is an extension of the standard Java's {@code ConcurrentMap}.</p>
 * <p>The heavy lifting of distributed data partitioning, network cluster discovery and
 * join, data replication, etc. is done by Hazelcast's Map implementation.</p>
 *
 * @author Dmitriy Kopylenko
 * @author Jonathan Johnson
 * @since 4.1.0
 */
@Slf4j
@Monitorable
public class HazelcastTicketRegistry extends AbstractTicketRegistry implements AutoCloseable, DisposableBean {
    private final HazelcastInstance hazelcastInstance;

    private final HazelcastTicketRegistryProperties properties;

    public HazelcastTicketRegistry(final CipherExecutor cipherExecutor,
                                   final TicketSerializationManager ticketSerializationManager,
                                   final TicketCatalog ticketCatalog,
                                   final ConfigurableApplicationContext applicationContext,
                                   final HazelcastInstance hazelcastInstance,
                                   final HazelcastTicketRegistryProperties properties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.hazelcastInstance = hazelcastInstance;
        this.properties = properties;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        return addTicket(ticket);
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        var ttl = ticket.getExpirationPolicy().getTimeToLive();
        /*
         * Valid values are integers between 0 and Integer.MAX VALUE. Its default value is 0,
         * which means infinite (no expiration and eviction).
         * If it is not 0, entries are evicted regardless of the set eviction policy described below.
         */
        if (ttl < 0 || ttl >= Integer.MAX_VALUE) {
            LOGGER.debug("The expiration policy of ticket [{}] is set to use a negative (i.e. infinite) ttl", ticket.getId());
            ttl = 0L;
        }

        LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(), ttl);
        val encTicket = encodeTicket(ticket);

        val metadata = ticketCatalog.find(ticket);
        val ticketMap = getTicketMapInstanceByMetadata(metadata);

        if (ticketMap != null) {
            val holder = HazelcastTicketDocument
                .builder()
                .id(encTicket.getId())
                .type(metadata.getImplementationClass().getName())
                .principal(digestIdentifier(getPrincipalIdFrom(ticket)))
                .timeToLive(ttl)
                .ticket(encTicket)
                .prefix(metadata.getPrefix())
                .service(ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService()) ? sat.getService().getId() : null)
                .attributes(collectAndDigestTicketAttributes(ticket))
                .build();
            ticketMap.set(encTicket.getId(), holder, ttl, TimeUnit.SECONDS);
            LOGGER.debug("Added ticket [{}] with ttl [{}s]", encTicket.getId(), ttl);
        } else {
            LOGGER.warn("Unable to locate ticket map for ticket metadata [{}]", metadata);
        }
        return ticket;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = digestIdentifier(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            return null;
        }
        val metadata = ticketCatalog.find(ticketId);
        if (metadata != null) {
            val map = getTicketMapInstanceByMetadata(metadata);
            if (map != null) {
                val document = map.get(encTicketId);
                if (document != null && document.getTicket() != null) {
                    val result = decodeTicket(document.getTicket());
                    if (predicate != null && predicate.test(result)) {
                        return result;
                    }
                }
                return null;
            } else {
                LOGGER.error("Unable to locate ticket map for ticket definition [{}]", metadata);
            }
        }
        LOGGER.warn("No ticket definition could be found in the catalog to match [{}]", ticketId);
        return null;
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        val encTicketId = digestIdentifier(ticketToDelete.getId());
        val metadata = ticketCatalog.find(ticketToDelete);
        val map = getTicketMapInstanceByMetadata(metadata);
        return map != null && map.remove(encTicketId) != null ? 1 : 0;
    }

    @Override
    public long deleteAll() {
        return ticketCatalog.findAll()
            .stream()
            .map(this::getTicketMapInstanceByMetadata)
            .filter(Objects::nonNull)
            .mapToInt(instance -> {
                val size = instance.size();
                instance.evictAll();
                instance.clear();
                return size;
            })
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return stream().collect(Collectors.toSet());
    }

    @Override
    public long countSessionsFor(final String principalId) {
        if (properties.getCore().isEnableJet()) {
            val md = ticketCatalog.find(TicketGrantingTicket.PREFIX);
            val sql = String.format("SELECT COUNT(*) FROM %s WHERE principal=?", md.getProperties().getStorageName());
            LOGGER.debug("Executing SQL query [{}]", sql);
            try (val results = hazelcastInstance.getSql().execute(sql, digestIdentifier(principalId))) {
                return results.iterator().next().getObject(0);
            }
        }
        return super.countSessionsFor(principalId);
    }

    @Override
    public long countTicketsFor(final Service service) {
        if (properties.getCore().isEnableJet()) {
            return ticketCatalog.findAll()
                .stream()
                .mapToLong(ticketDefinition -> {
                    val sql = String.format("SELECT COUNT(*) FROM %s WHERE service=?",
                        ticketDefinition.getProperties().getStorageName());
                    LOGGER.debug("Executing SQL query [{}]", sql);
                    try (val results = hazelcastInstance.getSql().execute(sql, service.getId())) {
                        return results.iterator().next().getObject(0);
                    }
                })
                .sum();
        }
        return super.countTicketsFor(service);
    }

    @Override
    public long sessionCount() {
        if (properties.getCore().isEnableJet()) {
            val md = ticketCatalog.find(TicketGrantingTicket.PREFIX);
            val sql = String.format("SELECT COUNT(*) FROM %s", md.getProperties().getStorageName());
            LOGGER.debug("Executing SQL query [{}]", sql);
            try (val results = hazelcastInstance.getSql().execute(sql)) {
                return results.iterator().next().getObject(0);
            }
        }
        return super.sessionCount();
    }

    @Override
    public long serviceTicketCount() {
        if (properties.getCore().isEnableJet()) {
            val md = ticketCatalog.find(ServiceTicket.PREFIX);
            val sql = String.format("SELECT COUNT(*) FROM %s", md.getProperties().getStorageName());
            LOGGER.debug("Executing SQL query [{}]", sql);
            try (val results = hazelcastInstance.getSql().execute(sql)) {
                return results.iterator().next().getObject(0);
            }
        }
        return super.serviceTicketCount();
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        if (properties.getCore().isEnableJet()) {
            val md = ticketCatalog.find(TicketGrantingTicket.PREFIX);
            val ticketMapInstance = getTicketMapInstance(md.getProperties().getStorageName());
            val queryBuilder = new ArrayList<String>();
            queryAttributes.forEach((key, values) -> {
                val valuesAsString = values.stream()
                    .map(Object::toString)
                    .map(this::digestIdentifier)
                    .map(singleValue -> String.format("'%s'", singleValue))
                    .collect(Collectors.joining(","));
                val query = String.format("attributes[%s] IN (%s)", digestIdentifier(key), valuesAsString);
                queryBuilder.add(query);
            });
            val query = '(' + String.join(" OR ", queryBuilder) + ") AND "
                + String.format("prefix='%s'", md.getPrefix());
            LOGGER.debug("Executing SQL query [{}]", query);
            val results = ticketMapInstance.values(Predicates.sql(query));
            return results.stream()
                .map(row -> decodeTicket(row.getTicket()))
                .filter(ticket -> !ticket.isExpired());
        }
        return super.getSessionsWithAttributes(queryAttributes);
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        if (properties.getCore().isEnableJet()) {
            val md = ticketCatalog.find(TicketGrantingTicket.PREFIX);
            val sql = String.format("SELECT * FROM %s WHERE principal=?", md.getProperties().getStorageName());
            LOGGER.debug("Executing SQL query [{}]", sql);
            try (val results = hazelcastInstance.getSql().execute(sql, digestIdentifier(principalId))) {
                return StreamSupport.stream(results.spliterator(), false)
                    .map(row -> {
                        val ticket = (Ticket) row.getObject("ticket");
                        return decodeTicket(ticket);
                    })
                    .filter(ticket -> !ticket.isExpired())
                    .toList()
                    .stream();
            }
        }
        return super.getSessionsFor(principalId);
    }

    @Override
    public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        return ticketCatalog
            .findAll()
            .stream()
            .map(metadata -> getTicketMapInstanceByMetadata(metadata).values())
            .flatMap(tickets -> tickets.stream().map(HazelcastTicketDocument::getTicket))
            .skip(criteria.getFrom())
            .limit(criteria.getCount())
            .map(this::decodeTicket);
    }
    
    /**
     * Make sure we shutdown HazelCast when the context is destroyed.
     */
    public void shutdown() {
        FunctionUtils.doAndHandle(__ -> {
            LOGGER.info("Shutting down Hazelcast instance [{}]", hazelcastInstance.getConfig().getInstanceName());
            hazelcastInstance.shutdown();
        });
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    public void close() {
        shutdown();
    }

    private IMap<String, HazelcastTicketDocument> getTicketMapInstanceByMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating map name [{}] for ticket definition [{}]", mapName, metadata);
        return getTicketMapInstance(mapName);
    }

    private IMap<String, HazelcastTicketDocument> getTicketMapInstance(
        @NonNull final String mapName) {
        return FunctionUtils.doAndHandle(() -> {
            val inst = hazelcastInstance.<String, HazelcastTicketDocument>getMap(mapName);
            LOGGER.debug("Located Hazelcast map instance [{}]", mapName);
            return inst;
        });
    }
}
