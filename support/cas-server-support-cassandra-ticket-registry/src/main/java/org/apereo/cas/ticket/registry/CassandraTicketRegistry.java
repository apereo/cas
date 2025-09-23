package org.apereo.cas.ticket.registry;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.ticketregistry.CassandraTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.cassandra.core.cql.BeanPropertyRowMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CassandraTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @author doomviking
 * @since 6.1.0
 */
@Slf4j
public class CassandraTicketRegistry extends AbstractTicketRegistry implements DisposableBean, InitializingBean {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final CassandraSessionFactory cassandraSessionFactory;

    private final CassandraTicketRegistryProperties properties;

    public CassandraTicketRegistry(final CipherExecutor cipherExecutor,
                                   final TicketSerializationManager ticketSerializationManager,
                                   final TicketCatalog ticketCatalog,
                                   final ConfigurableApplicationContext applicationContext,
                                   final CassandraSessionFactory cassandraSessionFactory,
                                   final CassandraTicketRegistryProperties properties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.cassandraSessionFactory = cassandraSessionFactory;
        this.properties = properties;
    }


    private static int getTimeToLive(final Ticket ticket) {
        val timeToLive = ticket.getExpirationPolicy().getTimeToLive();
        val ttl = Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
        if (ttl >= CassandraSessionFactory.MAX_TTL) {
            return CassandraSessionFactory.MAX_TTL;
        }
        return ttl.intValue();
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        LOGGER.trace("Locating ticket [{}]", ticketId);
        val encodedTicketId = digestIdentifier(ticketId);
        if (StringUtils.isBlank(encodedTicketId)) {
            LOGGER.debug("Ticket id [{}] could not be found", ticketId);
            return null;
        }

        val definition = ticketCatalog.find(ticketId);
        if (definition == null) {
            LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
            return null;
        }

        val holder = findCassandraTicketBy(definition, encodedTicketId);
        if (holder.isEmpty()) {
            LOGGER.debug("Ticket [{}] could not be found in Cassandra", encodedTicketId);
            return null;
        }

        val document = holder.iterator().next();
        val object = deserializeTicket(document.getData(), document.getType());
        val result = decodeTicket(object);
        return FunctionUtils.doAndReturn(result != null && predicate.test(result), () -> result, () -> {
            LOGGER.trace("The condition enforced by the predicate [{}] cannot successfully accept/test the ticket id [{}]", encodedTicketId,
                predicate.getClass().getSimpleName());
            return null;
        });
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        return addTicketToCassandra(ticket, true);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        return addTicketToCassandra(ticket, false);
    }

    @Override
    public Collection<Ticket> getTickets() {
        return ticketCatalog.findAll()
            .stream()
            .map(definition -> {
                val results = findCassandraTicketBy(definition);
                return results
                    .stream()
                    .map(holder -> {
                        val result = deserializeTicket(holder.getData(), holder.getType());
                        return decodeTicket(result);
                    })
                    .collect(Collectors.toSet());
            })
            .flatMap(Set::stream)
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired())
            .collect(Collectors.toSet());
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        val ticketId = digestIdentifier(ticketToDelete.getId());
        LOGGER.debug("Deleting ticket [{}]", ticketId);
        val definition = ticketCatalog.find(ticketToDelete);
        val delete = QueryBuilder
            .deleteFrom(properties.getKeyspace(), definition.getProperties().getStorageName())
            .whereColumn("id").isEqualTo(QueryBuilder.literal(ticketId))
            .build()
            .setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
            .setTimeout(Beans.newDuration(properties.getTimeout()));
        cassandraSessionFactory.getCqlTemplate().execute(delete);
        return 1;
    }

    @Override
    public long deleteAll() {
        ticketCatalog.findAll()
            .stream()
            .map(definition -> QueryBuilder
                .truncate(properties.getKeyspace(), definition.getProperties().getStorageName())
                .build()
                .setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
                .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
                .setTimeout(Beans.newDuration(properties.getTimeout())))
            .forEach(delete -> {
                LOGGER.trace("Attempting to delete all via query [{}]", delete);
                cassandraSessionFactory.getCqlTemplate().execute(delete);
            });
        return -1;
    }

    @Override
    public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        return ticketCatalog
            .findAll()
            .stream()
            .flatMap(this::streamCassandraTicketBy)
            .skip(criteria.getFrom())
            .limit(criteria.getCount())
            .map(holder -> {
                val result = deserializeTicket(holder.getData(), holder.getType());
                return decodeTicket(result);
            });
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val metadata = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
        val queryList = new ArrayList<String>();
        queryAttributes.forEach((key, values) ->
            values.forEach(queryValue -> {
                var cql = "SELECT * FROM %s.%s WHERE prefix='%s' AND ".formatted(properties.getKeyspace(), metadata.getProperties().getStorageName(), metadata.getPrefix());
                cql += "attributes CONTAINS KEY '%s' AND attributes CONTAINS '%s' ALLOW FILTERING;".formatted(digestIdentifier(key), digestIdentifier(queryValue.toString()));
                queryList.add(cql);
            }));
        val rowMapper = new BeanPropertyRowMapper<>(CassandraTicketHolder.class, true);
        return queryList
            .stream()
            .flatMap(query -> cassandraSessionFactory.getCqlTemplate().queryForStream(query, rowMapper))
            .distinct()
            .map(holder -> {
                val result = deserializeTicket(holder.getData(), holder.getType());
                return decodeTicket(result);
            })
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public void destroy() throws Exception {
        cassandraSessionFactory.close();
    }

    @Override
    public void afterPropertiesSet() {
        createTablesIfNecessary();
    }


    private Collection<CassandraTicketHolder> findCassandraTicketBy(final TicketDefinition definition) {
        return findCassandraTicketBy(definition, null);
    }

    private Collection<CassandraTicketHolder> findCassandraTicketBy(final TicketDefinition definition, final String ticketId) {
        val builder = QueryBuilder.selectFrom(properties.getKeyspace(), definition.getProperties().getStorageName()).all();
        if (StringUtils.isNotBlank(ticketId)) {
            builder.whereColumn("id").isEqualTo(QueryBuilder.literal(ticketId)).limit(1);
        }
        val select = builder.build()
            .setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
            .setTimeout(Beans.newDuration(properties.getTimeout()));
        LOGGER.trace("Attempting to locate ticket via query [{}]", select);
        val rowMapper = new BeanPropertyRowMapper<>(CassandraTicketHolder.class, true);
        return cassandraSessionFactory.getCqlTemplate().query(select, rowMapper);
    }

    private Stream<CassandraTicketHolder> streamCassandraTicketBy(final TicketDefinition definition) {
        val builder = QueryBuilder.selectFrom(properties.getKeyspace(), definition.getProperties().getStorageName()).all();
        val select = builder.build()
            .setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
            .setTimeout(Beans.newDuration(properties.getTimeout()));
        LOGGER.trace("Attempting to locate ticket via query [{}]", select);
        val rowMapper = new BeanPropertyRowMapper<>(CassandraTicketHolder.class, true);
        return cassandraSessionFactory.getCqlTemplate().queryForStream(select, rowMapper);
    }

    private void createTablesIfNecessary() {
        val createNs = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class':'SimpleStrategy','replication_factor':1 };"
            .formatted(properties.getKeyspace()).stripIndent().strip();
        LOGGER.trace("Creating Cassandra keyspace with query [{}]", createNs);
        cassandraSessionFactory.getCqlTemplate().execute(createNs);

        ticketCatalog.findAll()
            .stream()
            .filter(metadata -> StringUtils.isNotBlank(metadata.getProperties().getStorageName()))
            .forEach(metadata -> {
                if (properties.isDropTablesOnStartup()) {
                    val drop = "DROP TABLE IF EXISTS %s.%s;".formatted(properties.getKeyspace(), metadata.getProperties().getStorageName());
                    LOGGER.trace("Dropping Cassandra table with query [{}]", drop);
                    cassandraSessionFactory.getCqlTemplate().execute(drop);
                }
                val createTable = "CREATE TABLE IF NOT EXISTS %s.%s(id text,type text,prefix text,attributes map<text, text>,data text, PRIMARY KEY(id,type));"
                    .formatted(properties.getKeyspace(), metadata.getProperties().getStorageName());
                LOGGER.trace("Creating Cassandra table with query [{}]", createTable);
                cassandraSessionFactory.getCqlTemplate().execute(createTable);

                cassandraSessionFactory.getCqlTemplate().execute("DROP INDEX IF EXISTS " + metadata.getProperties().getStorageName() + "_entries_index");
                val createIndexAttributeNames = "CREATE INDEX " + metadata.getProperties().getStorageName() + "_entries_index ON "
                                                + properties.getKeyspace() + '.' + metadata.getProperties().getStorageName() + " (ENTRIES(attributes));";
                LOGGER.trace("Creating Cassandra index with query [{}]", createIndexAttributeNames);
                cassandraSessionFactory.getCqlTemplate().execute(createIndexAttributeNames);

                cassandraSessionFactory.getCqlTemplate().execute("DROP INDEX IF EXISTS " + metadata.getProperties().getStorageName() + "_values_index");
                val createIndexAttributeValues = "CREATE INDEX " + metadata.getProperties().getStorageName() + "_values_index ON "
                                                 + properties.getKeyspace() + '.' + metadata.getProperties().getStorageName() + " (VALUES(attributes));";
                LOGGER.trace("Creating Cassandra index with query [{}]", createIndexAttributeValues);
                cassandraSessionFactory.getCqlTemplate().execute(createIndexAttributeValues);

                cassandraSessionFactory.getCqlTemplate().execute("DROP INDEX IF EXISTS " + metadata.getProperties().getStorageName() + "_keys_index");
                val createIndexAttributeNames3 = "CREATE INDEX " + metadata.getProperties().getStorageName() + "_keys_index ON "
                                                 + properties.getKeyspace() + '.' + metadata.getProperties().getStorageName() + " (KEYS(attributes));";
                LOGGER.trace("Creating Cassandra index with query [{}]", createIndexAttributeNames3);
                cassandraSessionFactory.getCqlTemplate().execute(createIndexAttributeNames3);
            });
    }


    private Ticket addTicketToCassandra(final Ticket ticket, final boolean inserting) throws Exception {
        LOGGER.debug("Adding ticket [{}]", ticket.getId());
        val metadata = ticketCatalog.find(ticket);
        LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
        val encTicket = encodeTicket(ticket);
        val data = ticketSerializationManager.serializeTicket(encTicket);
        val ttl = getTimeToLive(ticket);
        var statement = (SimpleStatement) null;

        val attributeMap = (Map<String, List>) collectAndDigestTicketAttributes(ticket);
        val attributesEncoded = attributeMap
            .entrySet()
            .stream()
            .map(entry -> {
                val entryValues = entry.getValue();
                val valueList = entryValues.stream().map(Object::toString).collect(Collectors.joining(","));
                return Pair.of(entry.getKey(), valueList);
            })
            .collect(Collectors.toMap(Pair::getKey, v -> v.getValue().toString()));

        if (inserting) {
            val document = CassandraTicketHolder.builder()
                .id(encTicket.getId())
                .data(data)
                .prefix(ticket.getPrefix())
                .type(encTicket.getClass().getName())
                .attributes(attributesEncoded)
                .build();
            val json = MAPPER.writeValueAsString(document);
            statement = QueryBuilder.insertInto(properties.getKeyspace(), metadata.getProperties().getStorageName())
                .json(json)
                .usingTtl(ttl)
                .build();
        } else {
            statement = QueryBuilder.update(properties.getKeyspace(), metadata.getProperties().getStorageName())
                .usingTtl(ttl)
                .setColumn("data", QueryBuilder.literal(data))
                .setColumn("attributes", QueryBuilder.literal(attributesEncoded))
                .whereColumn("id").isEqualTo(QueryBuilder.literal(encTicket.getId()))
                .whereColumn("type").isEqualTo(QueryBuilder.literal(encTicket.getClass().getName()))
                .build();
        }
        statement = statement.setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
            .setTimeout(Beans.newDuration(properties.getTimeout()));

        LOGGER.trace("Attempting to locate ticket via query [{}]", statement.getQuery());
        cassandraSessionFactory.getCqlTemplate().execute(statement);
        LOGGER.debug("Added ticket [{}]", encTicket.getId());
        return ticket;
    }
}
