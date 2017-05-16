package org.apereo.cas.dao;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apereo.cas.TicketSerializer;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Class responsible of running the queries against Cassandra.
 *
 * @author David Rodriguez
 *
 * @param <T> type that will be written to Cassandra. Should
 * be compatible with Cassandra, like String
 *
 * @since 5.1.0
 */
public class CassandraDao<T> implements NoSqlTicketRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraDao.class);
    private static final int FIRST_COLUMN_INDEX = 0;
    private static final long TEN_SECONDS = 10000L;
    private static final int TEN = 10;

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

    private final PreparedStatement insertExStmt;
    private final PreparedStatement deleteExStmt;
    private final PreparedStatement selectExStmt;
    private final PreparedStatement selectDateExStmt;

    private final PreparedStatement selectLrStmt;
    private final PreparedStatement updateLrStmt;

    private final Session session;

    public CassandraDao(final String contactPoints, final String username, final String password, final TicketSerializer<T> serializer,
                        final Class<T> typeToWriteToCassandra, final String tgtTable, final String stTable, final String expiryTable,
                        final String lastRunTable) {
        this.serializer = serializer;
        this.typeToWriteToCassandra = typeToWriteToCassandra;
        final Cluster cluster = Cluster.builder().addContactPoints(contactPoints.split(",")).withCredentials(username, password)
                .withProtocolVersion(ProtocolVersion.V3).build();

        this.session = cluster.connect();

        this.selectTgtStmt = session.prepare("select ticket from " + tgtTable + " where id = ?")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.insertTgtStmt = session.prepare("insert into " + tgtTable + " (id, ticket) values (?, ?) ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.deleteTgtStmt = session.prepare("delete from " + tgtTable + " where id = ?")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.updateTgtStmt = session.prepare("update " + tgtTable + " set ticket = ? where id = ? ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        this.selectStStmt = session.prepare("select ticket from " + stTable + " where id = ?")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.insertStStmt = session.prepare("insert into " + stTable + " (id, ticket) values (?, ?) ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.deleteStStmt = session.prepare("delete from " + stTable + " where id = ?")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.updateStStmt = session.prepare("update " + stTable + " set ticket = ? where id = ? ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        this.insertExStmt = session.prepare("insert into " + expiryTable + " (expiry_type, date_bucket, id) values ('EX', ?, ?) ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.deleteExStmt = session.prepare("delete from " + expiryTable + " where  expiry_type = 'EX' and date_bucket = ? ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.selectExStmt = session.prepare("select id from " + expiryTable + " where expiry_type = 'EX' and date_bucket = ? ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.selectDateExStmt = session.prepare("select date_bucket from " + expiryTable)
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        this.selectLrStmt = session.prepare("select last_run from " + lastRunTable + " where id = 'LASTRUN' ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.updateLrStmt = session.prepare("update " + lastRunTable + " set last_run = ? where id = 'LASTRUN' ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        final long lastRun = getLastRunTimestamp();
        final long currentTime = currentTimeBucket();
        if (lastRun == 0 || lastRun > currentTime) {
            updateLastRunTimestamp(currentTime);
        }
    }

    @Override
    public void addTicketGrantingTicket(final Ticket ticket) {
        LOGGER.debug("INSERTING TICKET {}", ticket.getId());
        session.execute(this.insertTgtStmt.bind(ticket.getId(), serializer.serializeTGT(ticket)));
        final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
        addTicketToExpiryBucket(ticket, calculateExpirationDate(tgt));
    }

    @Override
    public void addServiceTicket(final Ticket ticket) {
        LOGGER.debug("INSERTING TICKET {}", ticket.getId());
        session.execute(this.insertStStmt.bind(ticket.getId(), serializer.serializeST(ticket)));
    }

    @Override
    public boolean deleteTicketGrantingTicket(final String id) {
        LOGGER.debug("DELETING TICKET {}", id);
        session.execute(this.deleteTgtStmt.bind(id));
        return true;
    }

    @Override
    public boolean deleteServiceTicket(final String id) {
        LOGGER.debug("DELETING TICKET {}", id);
        session.executeAsync(this.deleteStStmt.bind(id));
        return true;
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket(final String id) {
        LOGGER.debug("READING TICKET {}", id);
        final Row row = session.execute(this.selectTgtStmt.bind(id)).one();
        if (row == null) {
            LOGGER.info("ticket {} not found", id);
            return null;
        }
        return serializer.deserializeTGT(row.get(FIRST_COLUMN_INDEX, typeToWriteToCassandra));
    }

    @Override
    public Ticket getServiceTicket(final String id) {
        LOGGER.debug("READING TICKET {}", id);
        final Row row = session.execute(this.selectStStmt.bind(id)).one();
        if (row == null) {
            LOGGER.info("ticket {} not found", id);
            return null;
        }
        return serializer.deserializeST(row.get(FIRST_COLUMN_INDEX, typeToWriteToCassandra));
    }

    @Override
    public void updateTicketGrantingTicket(final Ticket ticket) {
        LOGGER.debug("UPDATING TICKET {}", ticket.getId());
        session.execute(this.updateTgtStmt.bind(serializer.serializeTGT(ticket), ticket.getId()));
        final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
        addTicketToExpiryBucket(ticket, calculateExpirationDate(tgt));
    }

    @Override
    public void updateServiceTicket(final Ticket ticket) {
        LOGGER.debug("UPDATING TICKET {}", ticket.getId());
        session.execute(this.updateStStmt.bind(serializer.serializeST(ticket), ticket.getId()));
    }

    @Override
    public void addTicketToExpiryBucket(final Ticket ticket, final long expirationTimeInSeconds) {
        LOGGER.debug("adding to expiry bucket: Ticket: {}; expiry: {}", ticket.getId(), expirationTimeInSeconds / TEN);
        session.execute(this.insertExStmt.bind(expirationTimeInSeconds / TEN, ticket.getId()));
    }

    @Override
    public void removeRowFromTicketCleanerBucket(final long lastRun) {
        session.executeAsync(deleteExStmt.bind(lastRun));
    }

    @Override
    public Stream<TicketGrantingTicket> getExpiredTgts() {
        final long lastRun = getLastRunTimestamp();
        final long currentTime = currentTimeBucket();

        LOGGER.debug("Searching for expired tickets. LastRun: {}; CurrentTime: {}", lastRun, currentTime);

        return LongStream.rangeClosed(lastRun, currentTime)
                .mapToObj(time -> {
                    final Stream<TicketGrantingTicket> expiredTGTsIn = getExpiredTGTsIn(time);
                    removeRowFromTicketCleanerBucket(time);
                    updateLastRunTimestamp(time);
                    return expiredTGTsIn;
                })
                .flatMap(Function.identity());
    }

    @Override
    public long getLastRunTimestamp() {
        final Row row = session.execute(this.selectLrStmt.bind()).one();
        if (row == null) {
            final List<Row> all = session.execute(this.selectDateExStmt.bind()).all();
            return all.stream()
                    .mapToLong(r -> r.getLong(FIRST_COLUMN_INDEX))
                    .min()
                    .orElseGet(CassandraDao::currentTimeBucket);
        } else {
            return row.getLong(FIRST_COLUMN_INDEX);
        }
    }

    @Override
    public void updateLastRunTimestamp(final long timestamp) {
        session.execute(this.updateLrStmt.bind(timestamp));
    }

    private Stream<TicketGrantingTicket> getExpiredTGTsIn(final long lastRunBucket) {
        return session.execute(this.selectExStmt.bind(lastRunBucket)).all()
                .stream()
                .map(row -> row.getString("id"))
                .map(this::getTicketGrantingTicket)
                .filter(Objects::nonNull)
                .filter(Ticket::isExpired);
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
