package org.apereo.cas.ticket.registry;

import module java.base;
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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

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
public class HazelcastTicketRegistry extends AbstractTicketRegistry {
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

    /**
     * Adds the ticket with the time-to-live its own expiration policy gives it.
     * An entry with a time-to-live does not idle out; one without keeps the map's max-idle setting,
     * which is the default lifetime of the ticket type and would otherwise evict longer-lived tickets
     * such as remember-me sessions.
     *
     * @param ticket the ticket
     * @return the ticket
     * @throws Exception the exception
     */
    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val ttl = getTimeToLive(ticket);
        val maxIdle = ttl > 0 ? 0L : -1L;
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
            ticketMap.set(encTicket.getId(), holder, ttl, TimeUnit.SECONDS, maxIdle, TimeUnit.SECONDS);
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
        return deleteSingleTicket(ticketToDelete.getId());
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        val encTicketId = digestIdentifier(ticketId);
        val metadata = ticketCatalog.find(ticketId);
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
    public long deleteTicketsFor(final String principalId) {
        if (properties.getCore().isEnableJet()) {
            return ticketCatalog.findAll()
                .stream()
                .mapToLong(ticketDefinition -> {
                    val storageName = ticketDefinition.getProperties().getStorageName();
                    val digestedPrincipal = digestIdentifier(principalId);
                    val countSql = String.format("SELECT COUNT(*) FROM %s WHERE principal=?", storageName);
                    LOGGER.debug("Executing SQL count query [{}]", countSql);
                    try (val countResult = hazelcastInstance.getSql().execute(countSql, digestedPrincipal)) {
                        val toBeDeleted = countResult.iterator().next().<Long>getObject(0);
                        if (toBeDeleted > 0) {
                            val deleteSql = String.format("DELETE FROM %s WHERE principal=?", storageName);
                            LOGGER.debug("Executing SQL delete query [{}]", deleteSql);
                            try (val _ = hazelcastInstance.getSql().execute(deleteSql, digestedPrincipal)) {
                                LOGGER.debug("Total tickets deleted are [{}]", toBeDeleted);
                            }
                        }
                        return toBeDeleted;
                    }
                })
                .sum();
        }
        return super.deleteTicketsFor(principalId);
    }

    /**
     * Finds sessions by attribute through typed predicates, so attribute values, which may come
     * from requests such as logout requests of external identity providers, are never parsed as part of a query.
     *
     * @param queryAttributes the query attributes
     * @return the matching sessions
     */
    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        if (properties.getCore().isEnableJet()) {
            if (queryAttributes.isEmpty()) {
                return Stream.empty();
            }
            val md = ticketCatalog.find(TicketGrantingTicket.PREFIX);
            val ticketMapInstance = getTicketMapInstance(md.getProperties().getStorageName());
            val predicates = queryAttributes.entrySet()
                .stream()
                .map(entry -> Predicates.<String, HazelcastTicketDocument>in(
                    "attributes[%s]".formatted(digestIdentifier(entry.getKey())),
                    entry.getValue().stream().map(Object::toString).map(this::digestIdentifier).toArray(String[]::new)))
                .toList();
            LOGGER.debug("Querying sessions by attributes [{}]", queryAttributes.keySet());
            val results = ticketMapInstance.values(Predicates.or(predicates.toArray(com.hazelcast.query.Predicate[]::new)));
            return results.stream()
                .map(row -> decodeTicket(row.getTicket()))
                .filter(Objects::nonNull)
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
        val ticketDefinitions = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class);
        val digestedPrincipal = digestIdentifier(principalId);
        return ticketDefinitions
            .stream()
            .map(this::getTicketMapInstanceByMetadata)
            .filter(Objects::nonNull)
            .flatMap(ticketMap -> ticketMap.values(Predicates.equal("principal", digestedPrincipal)).stream())
            .map(doc -> decodeTicket(doc.getTicket()))
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired());
    }

    /**
     * Streams tickets of all ticket maps, in catalog order and by key within a map.
     * The criteria apply to the whole registry, not to each map: {@code from} tickets are skipped
     * and at most {@code count} tickets are returned.
     *
     * @param criteria the criteria
     * @return the tickets
     */
    @Override
    public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        val ticketMaps = ticketCatalog
            .findAll()
            .stream()
            .map(this::getTicketMapInstanceByMetadata)
            .filter(Objects::nonNull)
            .toList();
        val documents = criteria.isInfiniteCount()
            ? ticketMaps.stream().flatMap(ticketMap -> ticketMap.values().stream()).skip(Math.max(criteria.getFrom(), 0L))
            : pageTicketDocuments(ticketMaps, Math.max(criteria.getFrom(), 0L), Math.toIntExact(Math.min(criteria.getCount(), Integer.MAX_VALUE)));
        return documents
            .map(HazelcastTicketDocument::getTicket)
            .map(this::decodeTicket);
    }

    /**
     * Time-to-live of the ticket in seconds, as its expiration policy gives it for this ticket, so delegating
     * policies such as remember-me apply. It never reaches past the policy's maximum expiration time, so an
     * update does not start the time-to-live over. Zero means the entry does not expire.
     *
     * @param ticket the ticket
     * @return the time to live in seconds
     */
    private static long getTimeToLive(final Ticket ticket) {
        val expirationPolicy = ticket.getExpirationPolicy();
        val timeToLive = expirationPolicy.getTimeToLive(ticket);
        if (timeToLive == null || timeToLive <= 0 || timeToLive >= Integer.MAX_VALUE) {
            LOGGER.debug("The expiration policy of ticket [{}] does not give a ttl the registry can use; the entry does not expire", ticket.getId());
            return 0L;
        }
        val maximumExpirationTime = expirationPolicy.toMaximumExpirationTime(ticket);
        if (maximumExpirationTime != null) {
            val remaining = Math.ceilDiv(Duration.between(ZonedDateTime.now(expirationPolicy.getClock()), maximumExpirationTime).toMillis(), 1000L);
            if (remaining > 0) {
                return Math.min(timeToLive, remaining);
            }
        }
        return timeToLive;
    }

    /**
     * Pages through the ticket maps in order: whole maps are skipped by size, and the first map that
     * holds tickets past {@code from} is read with a paging predicate, followed by the next maps until
     * {@code count} tickets are collected.
     *
     * @param ticketMaps the ticket maps
     * @param from       the number of tickets to skip
     * @param count      the maximum number of tickets
     * @return the ticket documents
     */
    private static Stream<HazelcastTicketDocument> pageTicketDocuments(final List<IMap<String, HazelcastTicketDocument>> ticketMaps,
                                                                       final long from, final int count) {
        val documents = new ArrayList<HazelcastTicketDocument>();
        var toSkip = from;
        val maps = ticketMaps.iterator();
        while (maps.hasNext() && documents.size() < count) {
            val ticketMap = maps.next();
            val size = ticketMap.size();
            if (toSkip >= size) {
                toSkip -= size;
            } else {
                val pageSize = count - documents.size();
                val pagePredicate = Predicates.pagingPredicate(Map.Entry.<String, HazelcastTicketDocument>comparingByKey(), pageSize);
                pagePredicate.setPage(Math.toIntExact(toSkip / pageSize));
                var offset = toSkip % pageSize;
                var page = ticketMap.values(pagePredicate);
                while (!page.isEmpty() && documents.size() < count) {
                    page.stream().skip(offset).limit(count - documents.size()).forEach(documents::add);
                    offset = 0;
                    pagePredicate.nextPage();
                    page = documents.size() < count ? ticketMap.values(pagePredicate) : List.of();
                }
                toSkip = 0;
            }
        }
        return documents.stream();
    }

    private @Nullable IMap<String, HazelcastTicketDocument> getTicketMapInstanceByMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating map name [{}] for ticket definition [{}]", mapName, metadata);
        return getTicketMapInstance(mapName);
    }

    private @Nullable IMap<String, HazelcastTicketDocument> getTicketMapInstance(@NonNull final String mapName) {
        return FunctionUtils.doAndHandle(() -> {
            val inst = hazelcastInstance.<String, HazelcastTicketDocument>getMap(mapName);
            LOGGER.debug("Located Hazelcast map instance [{}]", mapName);
            return inst;
        });
    }
}
