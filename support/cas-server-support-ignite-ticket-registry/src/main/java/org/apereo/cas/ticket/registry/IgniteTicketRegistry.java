package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteServer;
import org.apache.ignite.table.Tuple;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link IgniteTicketRegistry}.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
@Slf4j
@ToString(callSuper = true)
public class IgniteTicketRegistry extends AbstractTicketRegistry implements DisposableBean {
    private final IgniteProperties properties;

    private final IgniteServer igniteServer;
    private final Ignite ignite;

    public IgniteTicketRegistry(final CipherExecutor cipherExecutor,
                                final TicketSerializationManager ticketSerializationManager,
                                final TicketCatalog ticketCatalog,
                                final ConfigurableApplicationContext applicationContext,
                                final Ignite ignite,
                                final IgniteServer igniteServer,
                                final IgniteProperties properties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.properties = properties;
        this.ignite = ignite;
        this.igniteServer = igniteServer;
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        val encodedTicket = encodeTicket(ticket);
        val metadata = ticketCatalog.find(ticket);

        val table = ignite.tables().table(metadata.getProperties().getStorageName());
        val kvView = table.keyValueView();

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

        val expirationTime = ticket.getExpirationPolicy().toMaximumExpirationTime(ticket);
        val entry = Tuple.create()
            .set("type", metadata.getImplementationClass().getName())
            .set("principal", digestIdentifier(getPrincipalIdFrom(ticket)))
            .set("prefix", metadata.getPrefix())
            .set("expire_at", expirationTime != null ? expirationTime.toLocalDateTime() : null)
            .set("attributes", attributesEncoded);

        if (ticket instanceof final EncodedTicket encoded) {
            entry.set("ticket", encoded.getEncodedTicket());
        } else {
            entry.set("ticket", getSerializeAndEncode(ticket));
        }
        kvView.put(null, Tuple.create().set("id", encodedTicket.getId()), entry);
        return ticket;
    }

    @Override
    public long deleteAll() {
        return ticketCatalog
            .findAll()
            .stream()
            .mapToLong(definition -> {
                val sql = "DELETE FROM " + definition.getProperties().getStorageName();
                try (val rs = ignite.sql().execute(null, sql)) {
                    return rs.affectedRows();
                }
            })
            .sum();
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketId) {
        val encTicketId = digestIdentifier(ticketId.getId());
        val metadata = ticketCatalog.find(ticketId);
        if (metadata != null) {
            val sql = "DELETE FROM %s where id=?".formatted(metadata.getProperties().getStorageName());
            try (val rs = ignite.sql().execute(null, sql, encTicketId)) {
                return rs.affectedRows();
            }
        }
        return 0;
    }

    @Override
    public @Nullable Ticket getTicket(final String ticketIdToGet, final Predicate<Ticket> predicate) {
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
        val table = ignite.tables().table(metadata.getProperties().getStorageName());
        val kvView = table.keyValueView();
        val keyTuple = Tuple.create().set("id", ticketId);
        val valueTuple = kvView.get(null, keyTuple);
        if (valueTuple == null) {
            LOGGER.debug("No ticket by id [{}] is found in the ignite ticket registry", ticketIdToGet);
            return null;
        }
        val ticketBytes = valueTuple.bytesValue("ticket");
        val result = decodeAndDeserialize(ticketBytes);
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
        return ticketCatalog
            .findAll()
            .stream()
            .flatMap(definition -> {
                val sql = "SELECT * FROM %s ORDER BY id ASC LIMIT ? OFFSET ?".formatted(definition.getProperties().getStorageName());
                try (val rs = ignite.sql().execute(null, sql, criteria.getCount(), criteria.getFrom())) {
                    return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(rs, Spliterator.ORDERED),
                        false
                    );
                }
            })
            .map(object -> decodeAndDeserialize(object.bytesValue("ticket")))
            .filter(Objects::nonNull);
    }

    @Override
    public long sessionCount() {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val sql = "SELECT COUNT(id) AS TOTAL FROM " + metadata.getProperties().getStorageName();
        try (val rs = ignite.sql().execute(null, sql)) {
            return rs.hasNext() ? rs.next().longValue("TOTAL") : 0;
        }
    }

    @Override
    public long serviceTicketCount() {
        val metadata = ticketCatalog.findTicketDefinition(ServiceTicket.class).orElseThrow();
        val sql = "SELECT COUNT(id) AS TOTAL FROM " + metadata.getProperties().getStorageName();
        try (val rs = ignite.sql().execute(null, sql)) {
            return rs.hasNext() ? rs.next().longValue("TOTAL") : 0;
        }
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val sql = "SELECT * FROM %s where principal=?".formatted(metadata.getProperties().getStorageName());
        try (val rs = ignite.sql().execute(null, sql, digestIdentifier(principalId))) {
            val rowStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(rs, Spliterator.ORDERED),
                false
            );
            return rowStream
                .map(row -> {
                    val bytes = row.bytesValue("ticket");
                    return decodeAndDeserialize(bytes);
                })
                .filter(Objects::nonNull)
                .onClose(rs::close);
        }
    }

    @Override
    public long deleteTicketsFor(final String principalId) {
        return ticketCatalog.findAll()
            .stream()
            .mapToLong(ticketDefinition -> {
                val sql = "DELETE FROM %s where principal=?".formatted(ticketDefinition.getProperties().getStorageName());
                try (val rs = ignite.sql().execute(null, sql, digestIdentifier(principalId))) {
                    return rs.affectedRows();
                }
            })
            .sum();
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val sql = new StringBuilder(String.format("SELECT * FROM %s WHERE prefix='%s' AND (",
            metadata.getProperties().getStorageName(), metadata.getPrefix()));
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
        val query = sql.toString();
        try (val rs = ignite.sql().execute(null, query)) {
            val rowStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(rs, Spliterator.ORDERED),
                false
            );
            return rowStream
                .map(row -> {
                    val bytes = row.bytesValue("ticket");
                    return decodeAndDeserialize(bytes);
                })
                .filter(Objects::nonNull)
                .onClose(rs::close);
        }
    }

    /**
     * Make sure we shutdown Ignite when the context is destroyed.
     */
    public void shutdown() {
        igniteServer.shutdown();
    }

    @Override
    public void destroy() {
        shutdown();
    }
}
