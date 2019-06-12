package org.apereo.cas.ticket.registry;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.ticketregistry.CassandraTicketRegistryProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;

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
public class CassandraTicketRegistry extends AbstractTicketRegistry implements DisposableBean {

    private final TicketCatalog ticketCatalog;
    private final CassandraTicketRegistryProperties properties;
    private final Session cassandraSession;
    private final TicketSerializationManager ticketSerializationManager;

    public CassandraTicketRegistry(final TicketCatalog ticketCatalog,
                                   final CassandraSessionFactory cassandraSessionFactory,
                                   final CassandraTicketRegistryProperties properties,
                                   final TicketSerializationManager ticketSerializationManager) {
        this.ticketCatalog = ticketCatalog;
        this.properties = properties;
        this.cassandraSession = cassandraSessionFactory.getSession();
        this.ticketSerializationManager = ticketSerializationManager;

        createTablesIfNecessary();
    }

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
        if (holder == null || holder.isEmpty()) {
            LOGGER.debug("Ticket [{}] could not be found in Cassandra", encodedTicketId);
            return null;
        }

        val deserialized = deserialize(holder.iterator().next());
        val result = decodeTicket(deserialized);
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
                        val deserialized = deserialize(holder);
                        return decodeTicket(deserialized);
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
            val delete = QueryBuilder.delete()
                .from(this.properties.getKeyspace(), definition.getProperties().getStorageName())
                .where(QueryBuilder.eq("id", ticketId))
                .setConsistencyLevel(getConsistencyLevel());
            LOGGER.trace("Attempting to delete ticket via query [{}]", delete);
            cassandraSession.execute(delete);
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
                val delete = QueryBuilder.truncate(this.properties.getKeyspace(), definition.getProperties().getStorageName());
                LOGGER.trace("Attempting to delete all via query [{}]", delete);
                cassandraSession.execute(delete);
            });
        return -1;
    }

    @Override
    public void destroy() {
        this.cassandraSession.close();
    }

    private Ticket deserialize(final CassandraTicketHolder holder) {
        if (holder == null) {
            LOGGER.trace("Unable to locate Cassandra ticket");
            return null;
        }
        return ticketSerializationManager.deserializeTicket(holder.getData(), holder.getType());
    }

    private static int getTimeToLive(final Ticket ticket) {
        val expirationPolicy = ticket.getExpirationPolicy();
        val ttl = Math.toIntExact(expirationPolicy.getTimeToLive());
        if (ttl >= CassandraSessionFactory.MAX_TTL) {
            return CassandraSessionFactory.MAX_TTL;
        }
        return ttl;
    }
    private Collection<CassandraTicketHolder> findCassandraTicketBy(final TicketDefinition definition) {
        return findCassandraTicketBy(definition, null);
    }
    
    private Collection<CassandraTicketHolder> findCassandraTicketBy(final TicketDefinition definition, final String ticketId) {
        val select = QueryBuilder.select().all()
            .from(this.properties.getKeyspace(), definition.getProperties().getStorageName());
        if (StringUtils.isNotBlank(ticketId)) {
            select.where(QueryBuilder.eq("id", ticketId))
                .limit(1);
        }
        select.setConsistencyLevel(getConsistencyLevel());
        LOGGER.trace("Attempting to locate ticket via query [{}]", select);
        val results = cassandraSession.execute(select);
        return results.all()
            .stream()
            .map(row -> {
                val id = row.get("id", String.class);
                val data = row.get("data", String.class);
                val type = row.get("type", String.class);
                return new CassandraTicketHolder(id, data, type);
            })
            .collect(Collectors.toList());
    }

    private void createTablesIfNecessary() {
        val createNs = new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
            .append(this.properties.getKeyspace()).append(" WITH replication = {")
            .append("'class':'SimpleStrategy','replication_factor':1")
            .append("};")
            .toString();
        LOGGER.trace("Creating Cassandra keyspace with query [{}]", createNs);
        this.cassandraSession.execute(createNs);

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
                    this.cassandraSession.execute(drop);
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
                this.cassandraSession.execute(createTable);
            });
    }

    private ConsistencyLevel getConsistencyLevel() {
        return ConsistencyLevel.valueOf(properties.getConsistencyLevel());
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
                .value("id", encTicket.getId())
                .value("data", data)
                .value("type", encTicket.getClass().getName())
                .using(QueryBuilder.ttl(ttl))
                .setConsistencyLevel(getConsistencyLevel());
        } else {
            insert = QueryBuilder.update(this.properties.getKeyspace(), metadata.getProperties().getStorageName())
                .with(QueryBuilder.set("data", data))
                .where(QueryBuilder.eq("id", encTicket.getId())).and(QueryBuilder.eq("type", encTicket.getClass().getName()))
                .using(QueryBuilder.ttl(ttl))
                .setConsistencyLevel(getConsistencyLevel());
        }
        LOGGER.trace("Attempting to locate ticket via query [{}]", insert);
        cassandraSession.execute(insert);
        LOGGER.debug("Added ticket [{}]", encTicket.getId());
    }
}
