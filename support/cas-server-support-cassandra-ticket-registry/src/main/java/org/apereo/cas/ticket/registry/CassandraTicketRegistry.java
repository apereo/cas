package org.apereo.cas.ticket.registry;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apereo.cas.TicketSerializer;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.apereo.cas.config.CassandraTicketRegistryTicketCatalogConfiguration.*;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public class CassandraTicketRegistry<T> extends AbstractTicketRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraTicketRegistry.class);

    private static final int FIRST_COLUMN_INDEX = 0;
    private static final long TEN_SECONDS = 10000L;
    private static final int TEN = 10;

    private final TicketCatalog ticketCatalog;
    private final TicketSerializer<T> serializer;
    private final Class<T> typeToWriteToCassandra;

    private final PreparedStatement insertTgtStmt;
    private final PreparedStatement updateTgtStmt;
    private final PreparedStatement selectTgtStmt;
    private final PreparedStatement deleteTgtStmt;

    private final PreparedStatement insertStStmt;
    private final PreparedStatement updateStStmt;
    private final PreparedStatement selectStStmt;
    private final PreparedStatement deleteStStmt;

    private final PreparedStatement selectExStmt;
    private final PreparedStatement selectDateExStmt;

    private final PreparedStatement selectLrStmt;
    private final PreparedStatement updateLrStmt;

    private final Session session;

    public CassandraTicketRegistry(final TicketCatalog ticketCatalog, final String contactPoints, final String username, final String password,
                                   final String keyspace, final TicketSerializer<T> serializer, final Class<T> typeToWriteToCassandra) {
        this.ticketCatalog = ticketCatalog;
        this.serializer = serializer;
        this.typeToWriteToCassandra = typeToWriteToCassandra;
        final Cluster cluster = Cluster.builder().addContactPoints(contactPoints.split(",")).withCredentials(username, password)
                .withProtocolVersion(ProtocolVersion.V3).build();

        this.session = cluster.connect(keyspace);

        this.selectTgtStmt = session.prepare("select ticket from " + TGT_TABLE + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.insertTgtStmt = session.prepare("insert into " + TGT_TABLE + " (id, ticket, ticket_granting_ticket_id, expiration_bucket) values (?, ?, ?, ?) ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.deleteTgtStmt = session.prepare("delete from " + TGT_TABLE + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.updateTgtStmt = session.prepare("update " + TGT_TABLE + " set ticket = ? where id = ? ").setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        this.selectStStmt = session.prepare("select ticket from " + ST_TABLE + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.insertStStmt = session.prepare("insert into " + ST_TABLE + " (id, ticket) values (?, ?) ").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.deleteStStmt = session.prepare("delete from " + ST_TABLE + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.updateStStmt = session.prepare("update " + ST_TABLE + " set ticket = ? where id = ? ").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        this.selectExStmt = session.prepare("select ticket, id from " + EXPIRY_TABLE + " where expiration_bucket = ? ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.selectDateExStmt = session.prepare("select expiration_bucket from " + EXPIRY_TABLE)
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        this.selectLrStmt = session.prepare("select last_run from " + LAST_RUN_TABLE + " where id = 'LASTRUN' ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.updateLrStmt = session.prepare("update " + LAST_RUN_TABLE + " set last_run = ? where id = 'LASTRUN' ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        final long lastRun = getLastRunTimestamp();
        final long currentTime = currentTimeBucket();
        if (lastRun == 0 || lastRun > currentTime) {
            updateLastRunTimestamp(currentTime);
        }
    }

    @Override
    public void addTicket(final Ticket ticket) {
        final String ticketId = ticket.getId();
        LOGGER.debug("Inserting ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final String storageName = ticketDefinition.getProperties().getStorageName();

        if (TGT_TABLE.equals(storageName)) {
            final String parentTgtId = ticket.getGrantingTicket() == null ? null : ticket.getGrantingTicket().getId();
            final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
            session.execute(this.insertTgtStmt.bind(ticket.getId(), serializer.serialize(ticket), parentTgtId, calculateExpirationDate(tgt) / TEN));
        } else if (ST_TABLE.equals(storageName)) {
            session.execute(this.insertStStmt.bind(ticket.getId(), serializer.serialize(ticket)));
        } else {
            LOGGER.error("Inserting unknown ticket type {}", ticket.getClass().getName());
        }
    }

    @Override
    public int deleteTicket(final String id) {
        if (deleteSingleTicket(id)) {
            return 1;
        }
        return 0;
    }

    @Override
    public long deleteAll() {
        return 0;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        LOGGER.debug("Deleting ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final String storageName = ticketDefinition.getProperties().getStorageName();
        if (TGT_TABLE.equals(storageName)) {
            return session.execute(this.deleteTgtStmt.bind(ticketId)).wasApplied();
        } else if (ST_TABLE.equals(storageName)) {
            session.executeAsync(this.deleteStStmt.bind(ticketId));
            return true;
        } else {
            LOGGER.error("Deleting unknown ticket type {}", ticketId);
            return false;
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        LOGGER.debug("Querying ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final PreparedStatement statement = getTicketQueryForStorageName(ticketDefinition);
        final Row row = session.execute(statement.bind(ticketId)).one();
        if (row == null) {
            LOGGER.info("ticket {} not found", ticketId);
            return null;
        }
        return serializer.deserialize(row.get(FIRST_COLUMN_INDEX, typeToWriteToCassandra), ticketDefinition.getImplementationClass());
    }

    private PreparedStatement getTicketQueryForStorageName(final TicketDefinition ticketDefinition) {
        final String storageName = ticketDefinition.getProperties().getStorageName();
        if (TGT_TABLE.equals(storageName)) {
            return this.selectTgtStmt;
        } else if (ST_TABLE.equals(storageName)) {
            return this.selectStStmt;
        }
        LOGGER.error("Requesting unknown ticket type {}", ticketDefinition.getImplementationClass());
        return null;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        final String ticketId = ticket.getId();
        LOGGER.debug("Updating ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final String storageName = ticketDefinition.getProperties().getStorageName();
        if (TGT_TABLE.equals(storageName)) {
            final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
            session.execute(this.updateTgtStmt.bind(serializer.serialize(ticket), ticket.getId(), calculateExpirationDate(tgt) / TEN));
        } else if (ST_TABLE.equals(storageName)) {
            session.execute(this.updateStStmt.bind(serializer.serialize(ticket), ticket.getId()));
        } else {
            LOGGER.error("Updating unknown ticket type {}", ticket.getClass().getName());
        }
        return ticket;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final long lastRun = getLastRunTimestamp();
        final long currentTime = currentTimeBucket();

        LOGGER.debug("Searching for expired tickets. LastRun: {}; CurrentTime: {}", lastRun, currentTime);

        return LongStream.rangeClosed(lastRun, currentTime)
                .mapToObj(time -> {
                    updateLastRunTimestamp(time);
                    return getExpiredTGTsIn(time);
                })
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    private Stream<Ticket> getExpiredTGTsIn(final long lastRunBucket) {
        final ResultSet resultSet = session.execute(this.selectExStmt.bind(lastRunBucket));
        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> serializer.deserialize(row.get(FIRST_COLUMN_INDEX, typeToWriteToCassandra),
                        ticketCatalog.find(row.getString(1)).getImplementationClass()))
                .filter(ticket -> Objects.nonNull(ticket) && ticket.isExpired());
    }

    private long getLastRunTimestamp() {
        final Row row = session.execute(this.selectLrStmt.bind()).one();
        if (row == null) {
            final List<Row> all = session.execute(this.selectDateExStmt.bind()).all();
            return all.stream()
                    .mapToLong(r -> r.getLong(FIRST_COLUMN_INDEX))
                    .min()
                    .orElseGet(CassandraTicketRegistry::currentTimeBucket);
        } else {
            return row.getLong(FIRST_COLUMN_INDEX);
        }
    }

    private void updateLastRunTimestamp(final long timestamp) {
        session.execute(this.updateLrStmt.bind(timestamp));
    }

    private static long currentTimeBucket() {
        return System.currentTimeMillis() / TEN_SECONDS;
    }

    private static long calculateExpirationDate(final TicketGrantingTicketImpl ticket) {
        final ZonedDateTime ticketTtl = ticket.getCreationTime().plusSeconds(ticket.getExpirationPolicy().getTimeToLive());
        final ZonedDateTime ticketTtk = ticket.getLastTimeUsed().plusSeconds(ticket.getExpirationPolicy().getTimeToIdle());

        return ticketTtl.isBefore(ticketTtk) ? ticketTtl.toEpochSecond() : ticketTtk.toEpochSecond();
    }
}
