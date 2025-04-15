package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.model.support.geode.GeodeProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.geode.cache.query.SelectResults;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is {@link GeodeTicketRegistry}, a ticket registry implementation
 * based on Apache Geode.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@ToString(callSuper = true)
public class GeodeTicketRegistry extends AbstractTicketRegistry implements DisposableBean {

    private final Map<String, GeodeCache> geodeCaches;
    private final GeodeProperties properties;

    public GeodeTicketRegistry(final CipherExecutor cipherExecutor,
                               final TicketSerializationManager ticketSerializationManager,
                               final TicketCatalog ticketCatalog,
                               final ConfigurableApplicationContext applicationContext,
                               final Map<String, GeodeCache> geodeCaches,
                               final GeodeProperties properties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.properties = properties;
        this.geodeCaches = Map.copyOf(geodeCaches);
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val encodedTicket = encodeTicket(ticket);
        val metadata = ticketCatalog.find(ticket);
        val cache = getCacheFromMetadata(metadata);
        LOGGER.debug("Adding ticket [{}] to the cache region [{}]", ticket.getId(), cache.region().getName());
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
        
        val document = GeodeTicketDocument.builder()
            .id(encodedTicket.getId())
            .kind(metadata.getImplementationClass().getName())
            .principal(digestIdentifier(getPrincipalIdFrom(ticket)))
            .ticket(encodedTicket)
            .prefix(metadata.getPrefix())
            .attributes(attributesEncoded)
            .build();
        cache.region().put(encodedTicket.getId(), document);
        return ticket;
    }

    @Override
    public long deleteAll() {
        return ticketCatalog.findAll()
            .stream()
            .map(this::getCacheFromMetadata)
            .filter(Objects::nonNull)
            .mapToLong(cache -> {
                val size = cache.region().size();
                cache.region().clear();
                return size;
            })
            .sum();
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketId) {
        val metadata = ticketCatalog.find(ticketId);
        if (metadata != null) {
            val cache = getCacheFromMetadata(metadata);
            val encTicketId = digestIdentifier(ticketId.getId());
            cache.region().destroy(encTicketId);
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
        val metadata = ticketCatalog.find(ticketIdToGet);
        if (metadata == null) {
            LOGGER.warn("Ticket [{}] is not registered in the catalog and is unrecognized", ticketIdToGet);
            return null;
        }
        val cache = getCacheFromMetadata(metadata);
        LOGGER.trace("Located cache region [{}] for ticket id [{}]", cache.region().getName(), ticketId);
        val ticket = cache.region().get(ticketId);
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
            .map(this::getCacheFromMetadata)
            .flatMap(cache -> {
                val it = cache.region().values().spliterator();
                return StreamSupport.stream(it, false);
            })
            .skip(criteria.getFrom())
            .limit(criteria.getCount())
            .map(object -> decodeTicket(object.getTicket()))
            .filter(Objects::nonNull);
    }

    @Override
    public long sessionCount() {
        return FunctionUtils.doUnchecked(() -> countTicketsFor(TicketGrantingTicket.class));
    }

    @Override
    public long serviceTicketCount() {
        return FunctionUtils.doUnchecked(() -> countTicketsFor(ServiceTicket.class));
    }

    @Override
    public long countSessionsFor(final String principalId) {
        return FunctionUtils.doUnchecked(() -> {
            val digestedId = digestIdentifier(principalId);
            return countTicketsFor(TicketGrantingTicket.class, "t.principal = $1", new Object[]{digestedId});
        });
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        return FunctionUtils.doUnchecked(() -> {
            val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
            val cache = getCacheFromMetadata(metadata);
            val queryString = "SELECT * FROM /%s t WHERE t.principal = $1".formatted(cache.region().getName());
            val query = cache.cache().getQueryService().newQuery(queryString);
            val results = (SelectResults<GeodeTicketDocument>) query.execute(digestIdentifier(principalId));
            return results
                .stream()
                .map(object -> decodeTicket(object.getTicket()))
                .filter(Objects::nonNull)
                .filter(ticket -> !ticket.isExpired());
        });
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val cache = getCacheFromMetadata(metadata);
        
        val sqlBuilder = new StringBuilder(String.format("SELECT * FROM /%s t WHERE t.prefix='%s' AND (",
            cache.region().getName(), metadata.getPrefix()));

        queryAttributes.forEach((key, values) ->
            values.forEach(queryValue ->
                sqlBuilder.append("attributes LIKE '%[")
                    .append(digestIdentifier(key))
                    .append(":{%")
                    .append(digestIdentifier(queryValue.toString()))
                    .append("%}]%' OR ")
            ));
        sqlBuilder.append("1=2);");
        
        val sql = sqlBuilder.toString();
        LOGGER.debug("Executing SQL query [{}]", sql);
        return FunctionUtils.doUnchecked(() -> {
            val query = cache.cache().getQueryService().newQuery(sql);
            val results = (SelectResults<GeodeTicketDocument>) query.execute();
            return results
                .stream()
                .map(object -> decodeTicket(object.getTicket()))
                .filter(Objects::nonNull)
                .filter(ticket -> !ticket.isExpired());
        });
    }

    @Override
    public void destroy() {
        geodeCaches.values().forEach(geode -> geode.cache().close());
    }

    private GeodeCache getCacheFromMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.trace("Locating cache name [{}] for ticket definition [{}]", mapName, metadata);
        return getCacheInstanceByName(mapName);
    }

    private Integer countTicketsFor(final Class<? extends Ticket> clazz) throws Exception {
        return countTicketsFor(clazz, "1=1", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private Integer countTicketsFor(final Class<? extends Ticket> clazz,
                                    final String where, final Object[] parameters) throws Exception {
        val metadata = ticketCatalog.findTicketDefinition(clazz).orElseThrow();
        val cache = getCacheFromMetadata(metadata);
        val queryString = "SELECT COUNT(t.id) FROM /%s t WHERE %s"
            .formatted(cache.region().getName(), where);
        val query = cache.cache().getQueryService().newQuery(queryString);
        val results = (SelectResults<Integer>) query.execute(parameters);
        return results.stream().findFirst().orElse(0);
    }

    private GeodeCache getCacheInstanceByName(final String name) {
        return geodeCaches.get(name);
    }
}
