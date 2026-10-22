package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
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
    private static final int STREAM_BATCH_SIZE = 500;

    private final HazelcastInstance hazelcastInstance;

    public HazelcastTicketRegistry(final CipherExecutor cipherExecutor,
                                   final TicketSerializationManager ticketSerializationManager,
                                   final TicketCatalog ticketCatalog,
                                   final ConfigurableApplicationContext applicationContext,
                                   final HazelcastInstance hazelcastInstance) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.hazelcastInstance = hazelcastInstance;
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
        val ticketMap = metadata != null ? getTicketMapInstanceByMetadata(metadata) : null;

        if (ticketMap != null) {
            val holder = HazelcastTicketDocument
                .builder()
                .principal(digestPrincipal(getPrincipalIdFrom(ticket)))
                .ticket(SerializationUtils.serialize(encTicket))
                .service(ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService()) ? digestIdentifier(sat.getService().getId()) : null)
                .attributes(TicketGrantingTicket.PREFIX.equals(metadata.getPrefix()) ? collectAttributeEntries(ticket) : ArrayUtils.EMPTY_STRING_ARRAY)
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
                if (document != null) {
                    val result = toTicket(document);
                    if (result != null && predicate != null && predicate.test(result)) {
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

    /**
     * Deletes the ticket. An encoded ticket is found in the map of its prefix under its own id, which is
     * already digested, such as one read back after encryption was turned off.
     *
     * @param ticketToDelete the ticket to delete
     * @return the number of tickets deleted
     */
    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        if (ticketToDelete instanceof final EncodedTicket encodedTicket) {
            return removeTicketDocument(ticketCatalog.find(encodedTicket.getPrefix()), encodedTicket.getId());
        }
        return deleteSingleTicket(ticketToDelete.getId());
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        return removeTicketDocument(ticketCatalog.find(ticketId), digestIdentifier(ticketId));
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

    /**
     * Counts the sessions of the principal through the principal index, matching the principal id regardless of case.
     *
     * @param principalId the principal id
     * @return the number of sessions
     */
    @Override
    public long countSessionsFor(final String principalId) {
        val ticketMap = getTicketMapInstanceByPrefix(TicketGrantingTicket.PREFIX);
        return ticketMap != null ? ticketMap.aggregate(Aggregators.count(), Predicates.equal("principal", digestPrincipal(principalId))) : 0;
    }

    @Override
    public long countTicketsFor(final Service service) {
        return getTicketMapInstances(ServiceAwareTicket.class)
            .mapToLong(ticketMap -> ticketMap.aggregate(Aggregators.count(), Predicates.equal("service", digestIdentifier(service.getId()))))
            .sum();
    }

    @Override
    public long sessionCount() {
        val ticketMap = getTicketMapInstanceByPrefix(TicketGrantingTicket.PREFIX);
        return ticketMap != null ? ticketMap.size() : 0;
    }

    @Override
    public long serviceTicketCount() {
        val ticketMap = getTicketMapInstanceByPrefix(ServiceTicket.PREFIX);
        return ticketMap != null ? ticketMap.size() : 0;
    }

    /**
     * Removes every ticket of the principal, matching the principal id regardless of case, in one indexed pass
     * over each map of authentication-aware tickets. The count is the number of tickets actually removed.
     *
     * @param principalId the principal id
     * @return the number of tickets removed
     */
    @Override
    public long deleteTicketsFor(final String principalId) {
        val predicate = Predicates.<String, HazelcastTicketDocument>equal("principal", digestPrincipal(principalId));
        return getTicketMapInstances(AuthenticationAwareTicket.class)
            .mapToLong(ticketMap -> countRemovals(ticketMap.executeOnEntries(new HazelcastTicketDocumentRemovalProcessor(), predicate)))
            .sum();
    }

    /**
     * Removes the service and proxy tickets of the ticket-granting ticket with one call per ticket map,
     * instead of one removal per ticket that also returns the removed document.
     *
     * @param ticket the ticket-granting ticket
     * @return the number of tickets removed
     */
    @Override
    protected int deleteServiceTickets(final TicketGrantingTicket ticket) {
        val services = ticket.getServices();
        if (services == null || services.isEmpty()) {
            return 0;
        }
        val keysByDefinition = services.keySet()
            .stream()
            .filter(ticketId -> ticketCatalog.find(ticketId) != null)
            .collect(Collectors.groupingBy(ticketCatalog::find, Collectors.mapping(this::digestIdentifier, Collectors.toSet())));
        val count = keysByDefinition.entrySet()
            .stream()
            .mapToLong(entry -> {
                val ticketMap = getTicketMapInstanceByMetadata(entry.getKey());
                return ticketMap != null ? countRemovals(ticketMap.executeOnKeys(entry.getValue(), new HazelcastTicketDocumentRemovalProcessor())) : 0;
            })
            .sum();
        return Math.toIntExact(count);
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
        val ticketMap = getTicketMapInstanceByPrefix(TicketGrantingTicket.PREFIX);
        if (queryAttributes.isEmpty() || ticketMap == null) {
            return Stream.empty();
        }
        val entries = queryAttributes.entrySet()
            .stream()
            .flatMap(entry -> entry.getValue()
                .stream()
                .map(value -> HazelcastTicketDocument.toAttributeEntry(digestIdentifier(entry.getKey()), digestIdentifier(value.toString()))))
            .toArray(String[]::new);
        if (entries.length == 0) {
            return Stream.empty();
        }
        LOGGER.debug("Querying sessions by attributes [{}]", queryAttributes.keySet());
        return ticketMap.values(Predicates.in("attributes[any]", entries))
            .stream()
            .map(this::toTicket)
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired());
    }

    /**
     * Finds the sessions of the principal through the principal index, matching the principal id regardless of case.
     *
     * @param principalId the principal id
     * @return the sessions
     */
    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val ticketMap = getTicketMapInstanceByPrefix(TicketGrantingTicket.PREFIX);
        if (ticketMap == null) {
            return Stream.empty();
        }
        return ticketMap.values(Predicates.equal("principal", digestPrincipal(principalId)))
            .stream()
            .map(this::toTicket)
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
            ? ticketMaps.stream().flatMap(HazelcastTicketRegistry::streamTicketDocuments).skip(Math.max(criteria.getFrom(), 0L))
            : pageTicketDocuments(ticketMaps, Math.max(criteria.getFrom(), 0L), Math.toIntExact(Math.min(criteria.getCount(), Integer.MAX_VALUE)));
        return documents
            .map(this::toTicket)
            .filter(Objects::nonNull);
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

    /**
     * Digest of the principal id in lower case, so principals are stored and looked up regardless of case.
     *
     * @param principalId the principal id
     * @return the digested principal id
     */
    private String digestPrincipal(final String principalId) {
        return digestIdentifier(StringUtils.lowerCase(principalId, Locale.ENGLISH));
    }

    /**
     * Streams the documents of a ticket map in batches: the keys are read first and the documents are fetched
     * a batch at a time as the stream is consumed, so the whole map is never held in memory at once.
     * Documents removed in the meantime are skipped.
     *
     * @param ticketMap the ticket map
     * @return the ticket documents
     */
    private static Stream<HazelcastTicketDocument> streamTicketDocuments(final IMap<String, HazelcastTicketDocument> ticketMap) {
        val keys = List.copyOf(ticketMap.keySet());
        val batches = (keys.size() + STREAM_BATCH_SIZE - 1) / STREAM_BATCH_SIZE;
        return IntStream.range(0, batches)
            .<List<String>>mapToObj(batch -> keys.subList(batch * STREAM_BATCH_SIZE, Math.min(keys.size(), (batch + 1) * STREAM_BATCH_SIZE)))
            .flatMap(batch -> ticketMap.getAll(new HashSet<>(batch)).values().stream());
    }

    private static long countRemovals(final Map<String, Boolean> results) {
        return results.values().stream().filter(Boolean.TRUE::equals).count();
    }

    /**
     * Logs out a single sign-on session that Hazelcast expired, as the ticket registry cleaner does for registries
     * that cannot expire tickets themselves: single logout is requested, the tickets the session still holds are
     * removed, and the session is reported as destroyed.
     *
     * @param document the document of the expired ticket
     */
    public void handleExpiredTicket(final HazelcastTicketDocument document) {
        if (toTicket(document) instanceof final TicketGrantingTicket ticketGrantingTicket) {
            val clientInfo = ClientInfoHolder.getClientInfo();
            applicationContext.publishEvent(new CasRequestSingleLogoutEvent(this, ticketGrantingTicket, clientInfo));
            FunctionUtils.doAndHandle(_ -> deleteTicket(ticketGrantingTicket));
            applicationContext.publishEvent(new CasTicketGrantingTicketDestroyedEvent(this, ticketGrantingTicket, clientInfo));
        }
    }

    private @Nullable Ticket toTicket(final HazelcastTicketDocument document) {
        return decodeTicket(SerializationUtils.deserialize(document.getTicket(), Ticket.class));
    }

    /**
     * Attribute entries of the ticket as kept in the document, one per attribute value; names and values are
     * digested when encryption is on, the same way query attributes are.
     *
     * @param ticket the ticket
     * @return the attribute entries
     */
    private String[] collectAttributeEntries(final Ticket ticket) {
        val attributes = (Map<String, Object>) collectAndDigestTicketAttributes(ticket);
        return attributes.entrySet()
            .stream()
            .flatMap(entry -> CollectionUtils.toCollection(entry.getValue())
                .stream()
                .filter(Objects::nonNull)
                .map(value -> HazelcastTicketDocument.toAttributeEntry(entry.getKey(), value.toString())))
            .toArray(String[]::new);
    }

    private long removeTicketDocument(final @Nullable TicketDefinition metadata, final String key) {
        if (metadata == null) {
            LOGGER.debug("No ticket definition could be found in the catalog for ticket [{}]", key);
            return 0;
        }
        val map = getTicketMapInstanceByMetadata(metadata);
        return map != null && map.remove(key) != null ? 1 : 0;
    }

    private Stream<IMap<String, HazelcastTicketDocument>> getTicketMapInstances(final Class<?> ticketType) {
        return ticketCatalog.findAll()
            .stream()
            .filter(definition -> ticketType.isAssignableFrom(definition.getImplementationClass()))
            .map(this::getTicketMapInstanceByMetadata)
            .filter(Objects::nonNull);
    }

    private @Nullable IMap<String, HazelcastTicketDocument> getTicketMapInstanceByPrefix(final String prefix) {
        val metadata = ticketCatalog.find(prefix);
        return metadata != null ? getTicketMapInstanceByMetadata(metadata) : null;
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
