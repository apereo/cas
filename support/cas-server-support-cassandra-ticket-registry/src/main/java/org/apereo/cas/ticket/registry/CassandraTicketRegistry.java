package org.apereo.cas.ticket.registry;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.ticketregistry.CassandraTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link CassandraTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @author doomviking
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CassandraTicketRegistry extends AbstractTicketRegistry implements DisposableBean, InitializingBean {

    private final TicketCatalog ticketCatalog;

    private final CassandraSessionFactory cassandraSessionFactory;

    private final CassandraTicketRegistryProperties properties;

    private final TicketSerializationManager ticketSerializationManager;

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        LOGGER.trace("Locating ticket  [{}]", ticketId);
        val encodedTicketId = encodeTicketId(ticketId);
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

        val object = deserialize(holder.iterator().next());
        val result = decodeTicket(object);
        if (result != null && predicate.test(result)) {
            return result;
        }
        LOGGER.trace("The condition enforced by the predicate [{}] cannot successfully accept/test the ticket id [{}]", encodedTicketId,
            predicate.getClass().getSimpleName());
        return null;
    }


    @Override
    public void addTicket(final Ticket ticket) {
        try {
            addTicketToCassandra(ticket, true);
        } catch (final Exception e) {
            LOGGER.error(String.format("Failed adding %s", ticket), e);
        }
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicketToCassandra(ticket, false);
        return ticket;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketCatalog.findAll()
            .stream()
            .map(definition -> {
                val results = findCassandraTicketBy(definition);
                return results
                    .stream()
                    .map(holder -> {
                        val result = deserialize(holder);
                        return decodeTicket(result);
                    })
                    .collect(Collectors.toSet());
            })
            .flatMap(Set::stream)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.debug("Deleting ticket [{}]", ticketId);
        try {
            val definition = this.ticketCatalog.find(ticketIdToDelete);
            if (definition == null) {
                LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
                return false;
            }
            val delete = QueryBuilder
                .deleteFrom(this.properties.getKeyspace(), definition.getProperties().getStorageName())
                .whereColumn("id").isEqualTo(QueryBuilder.literal(ticketId))
                .build()
                .setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
                .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
                .setTimeout(Beans.newDuration(properties.getTimeout()));
            cassandraSessionFactory.getCqlTemplate().execute(delete);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Failed deleting [{}]: [{}]", ticketId, e);
        }
        return false;
    }

    @Override
    public long deleteAll() {
        this.ticketCatalog.findAll()
            .forEach(definition -> {
                val delete = QueryBuilder
                    .truncate(this.properties.getKeyspace(), definition.getProperties().getStorageName())
                    .build()
                    .setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
                    .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
                    .setTimeout(Beans.newDuration(properties.getTimeout()));
                LOGGER.trace("Attempting to delete all via query [{}]", delete);
                cassandraSessionFactory.getCqlTemplate().execute(delete);
            });
        return -1;
    }

    @Override
    public void destroy() throws Exception {
        this.cassandraSessionFactory.close();
    }

    @Override
    public void afterPropertiesSet() {
        createTablesIfNecessary();
    }

    private Ticket deserialize(final CassandraTicketHolder holder) {
        if (holder == null) {
            LOGGER.trace("Unable to locate Cassandra ticket");
            return null;
        }
        return ticketSerializationManager.deserializeTicket(holder.getData(), holder.getType());
    }

    private static int getTimeToLive(final Ticket ticket) {
        val timeToLive = ticket.getExpirationPolicy().getTimeToLive();
        val ttl = Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
        if (ttl >= CassandraSessionFactory.MAX_TTL) {
            return CassandraSessionFactory.MAX_TTL;
        }
        return ttl.intValue();
    }

    private Collection<CassandraTicketHolder> findCassandraTicketBy(final TicketDefinition definition) {
        return findCassandraTicketBy(definition, null);
    }

    private Collection<CassandraTicketHolder> findCassandraTicketBy(final TicketDefinition definition, final String ticketId) {
        val builder = QueryBuilder.selectFrom(this.properties.getKeyspace(), definition.getProperties().getStorageName()).all();
        if (StringUtils.isNotBlank(ticketId)) {
            builder.whereColumn("id").isEqualTo(QueryBuilder.literal(ticketId)).limit(1);
        }
        val select = builder.build()
            .setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
            .setTimeout(Beans.newDuration(properties.getTimeout()));
        LOGGER.trace("Attempting to locate ticket via query [{}]", select);
        return cassandraSessionFactory.getCqlTemplate().query(select, (row, i) -> {
            val id = row.get("id", String.class);
            val data = row.get("data", String.class);
            val type = row.get("type", String.class);
            return new CassandraTicketHolder(id, data, type);
        });
    }

    private void createTablesIfNecessary() {
        val createNs = new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
            .append(this.properties.getKeyspace()).append(" WITH replication = {")
            .append("'class':'SimpleStrategy','replication_factor':1")
            .append("};")
            .toString();
        LOGGER.trace("Creating Cassandra keyspace with query [{}]", createNs);
        cassandraSessionFactory.getCqlTemplate().execute(createNs);

        ticketCatalog.findAll()
            .stream()
            .filter(metadata -> StringUtils.isNotBlank(metadata.getProperties().getStorageName()))
            .forEach(metadata -> {
                if (properties.isDropTablesOnStartup()) {
                    val drop = new StringBuilder("DROP TABLE IF EXISTS ")
                        .append(this.properties.getKeyspace())
                        .append('.')
                        .append(metadata.getProperties().getStorageName())
                        .append(';')
                        .toString();
                    LOGGER.trace("Dropping Cassandra table with query [{}]", drop);
                    cassandraSessionFactory.getCqlTemplate().execute(drop);
                }
                val createTable = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                    .append(this.properties.getKeyspace())
                    .append('.')
                    .append(metadata.getProperties().getStorageName())
                    .append('(')
                    .append("id text,")
                    .append("type text,")
                    .append("data text, ")
                    .append("PRIMARY KEY(id,type) ")
                    .append(");")
                    .toString();
                LOGGER.trace("Creating Cassandra table with query [{}]", createTable);
                cassandraSessionFactory.getCqlTemplate().execute(createTable);
            });
    }


    private void addTicketToCassandra(final Ticket ticket, final boolean inserting) {
        LOGGER.debug("Adding ticket [{}]", ticket.getId());
        val metadata = this.ticketCatalog.find(ticket);
        if (metadata == null) {
            LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
            return;
        }
        LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
        val encTicket = encodeTicket(ticket);
        val data = ticketSerializationManager.serializeTicket(encTicket);
        val ttl = getTimeToLive(ticket);
        var insert = (Statement) null;
        if (inserting) {
            insert = QueryBuilder.insertInto(this.properties.getKeyspace(), metadata.getProperties().getStorageName())
                .value("id", QueryBuilder.literal(encTicket.getId()))
                .value("data", QueryBuilder.literal(data))
                .value("type", QueryBuilder.literal(encTicket.getClass().getName()))
                .usingTtl(ttl)
                .build();
        } else {
            insert = QueryBuilder.update(this.properties.getKeyspace(), metadata.getProperties().getStorageName())
                .usingTtl(ttl)
                .setColumn("data", QueryBuilder.literal(data))
                .whereColumn("id").isEqualTo(QueryBuilder.literal(encTicket.getId()))
                .whereColumn("type").isEqualTo(QueryBuilder.literal(encTicket.getClass().getName()))
                .build();
        }
        insert = insert.setConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
            .setTimeout(Beans.newDuration(properties.getTimeout()));

        LOGGER.trace("Attempting to locate ticket via query [{}]", insert);
        cassandraSessionFactory.getCqlTemplate().execute(insert);
        LOGGER.debug("Added ticket [{}]", encTicket.getId());
    }
}
