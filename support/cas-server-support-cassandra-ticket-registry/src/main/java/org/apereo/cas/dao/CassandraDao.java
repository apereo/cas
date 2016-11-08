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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class CassandraDao implements NoSqlTicketRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraDao.class);

    private static final String KEYSPACE = "cas";

    private static final String TICKET_GRANTING_TICKET_TABLE = KEYSPACE + ".ticketgrantingticket";
    private static final String SERVICE_TICKET_TABLE = KEYSPACE + ".serviceticket";
    private static final String TICKET_EXPIRY_TABLE = KEYSPACE + ".ticket_cleaner";
    private static final String TICKET_CLEANER_LAST_RUN_TABLE = KEYSPACE + ".ticket_cleaner_lastrun";
    private static final int TICKET_COLUMN_INDEX = 0;

    private static final String INSERT_TGT = "insert into " + TICKET_GRANTING_TICKET_TABLE + " (id, ticket) values (?, ?) ";
    private static final String UPDATE_TGT = "update " + TICKET_GRANTING_TICKET_TABLE + " set ticket = ? where id = ? ";
    private static final String DELETE_TGT = "delete from " + TICKET_GRANTING_TICKET_TABLE + " where id = ?";
    private static final String SELECT_TGT = "select ticket from " + TICKET_GRANTING_TICKET_TABLE + " where id = ?";

    private static final String INSERT_ST = "insert into " + SERVICE_TICKET_TABLE + " (id, ticket) values (?, ?) ";
    private static final String UPDATE_ST = "update " + SERVICE_TICKET_TABLE + " set ticket = ? where id = ? ";
    private static final String DELETE_ST = "delete from " + SERVICE_TICKET_TABLE + " where id = ?";
    private static final String SELECT_ST = "select ticket from " + SERVICE_TICKET_TABLE + " where id = ?";

    private static final String INSERT_EX = "insert into " + TICKET_EXPIRY_TABLE + " (expiry_type, date_bucket, id) values ('EX', ?, ?) ";
    private static final String DELETE_EX = "delete from " + TICKET_EXPIRY_TABLE + " where  expiry_type = 'EX' and date_bucket = ? ";
    private static final String SELECT_EX = "select id from " + TICKET_EXPIRY_TABLE + " where expiry_type = 'EX' and date_bucket = ? ";

    private static final String UPDATE_LR = "update " + TICKET_CLEANER_LAST_RUN_TABLE + " set last_run = ? where id = 'LASTRUN' ";
    private static final String SELECT_LR = "select last_run from " + TICKET_CLEANER_LAST_RUN_TABLE + " where id = 'LASTRUN' ";
    private static final long TEN_SECONDS = 10000L;

    private int maxTicketDuration;
    private final long maxTgtsToLoad;
    private final TicketSerializer serializer;

    private PreparedStatement insertTgtStmt;
    private PreparedStatement updateTgtStmt;
    private PreparedStatement selectTgtStmt;
    private PreparedStatement deleteTgtStmt;

    private PreparedStatement insertStStmt;
    private PreparedStatement updateStStmt;
    private PreparedStatement selectStStmt;
    private PreparedStatement deleteStStmt;

    private PreparedStatement insertExStmt;
    private PreparedStatement deleteExStmt;
    private PreparedStatement selectExStmt;

    private PreparedStatement selectLrStmt;
    private PreparedStatement updateLrStmt;

    private Session session;

    public CassandraDao(final String contactPoints, final int maxTicketDuration, final String username, final String password, final long maxTgtsToLoad, final TicketSerializer serializer) {
        this.maxTicketDuration = maxTicketDuration;
        this.maxTgtsToLoad = maxTgtsToLoad;
        this.serializer = serializer;
        final Cluster cluster = Cluster.builder().addContactPoints(contactPoints.split(",")).withCredentials(username, password)
                .withProtocolVersion(ProtocolVersion.V3).build();

        this.session = cluster.connect(KEYSPACE);

        this.selectTgtStmt = session.prepare(SELECT_TGT).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.insertTgtStmt = session.prepare(INSERT_TGT).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.deleteTgtStmt = session.prepare(DELETE_TGT).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.updateTgtStmt = session.prepare(UPDATE_TGT).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        this.selectStStmt = session.prepare(SELECT_ST).setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.insertStStmt = session.prepare(INSERT_ST).setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.deleteStStmt = session.prepare(DELETE_ST).setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.updateStStmt = session.prepare(UPDATE_ST).setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        this.insertExStmt = session.prepare(INSERT_EX).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.deleteExStmt = session.prepare(DELETE_EX).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.selectExStmt = session.prepare(SELECT_EX).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        this.selectLrStmt = session.prepare(SELECT_LR).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.updateLrStmt = session.prepare(UPDATE_LR).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
    }

    @Override
    public void addTicketGrantingTicket(final Ticket ticket) {
        LOGGER.debug("INSERTING TICKET {}", ticket.getId());
        // TODO: should we create a serializeTGT method?
        session.execute(this.insertTgtStmt.bind(ticket.getId(), serializer.serialize(ticket)));
    }

    @Override
    public void addServiceTicket(final Ticket ticket) {
        LOGGER.debug("INSERTING TICKET {}", ticket.getId());
        // TODO: should we create a serializeST method?
        session.execute(this.insertStStmt.bind(ticket.getId(), serializer.serialize(ticket)));
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
        return serializer.deserializeTGT(row.get(TICKET_COLUMN_INDEX, ByteBuffer.class));
    }

    @Override
    public Ticket getServiceTicket(final String id) {
        LOGGER.debug("READING TICKET {}", id);
        final Row row = session.execute(this.selectStStmt.bind(id)).one();
        if (row == null) {
            LOGGER.info("ticket {} not found", id);
            return null;
        }
        return serializer.deserializeST(row.get(TICKET_COLUMN_INDEX, ByteBuffer.class));
    }

    @Override
    public void updateTicketGrantingTicket(final Ticket ticket) {
        LOGGER.debug("UPDATING TICKET {}", ticket.getId());
        session.execute(this.updateTgtStmt.bind(serializer.serialize(ticket), ticket.getId()));
    }

    @Override
    public void updateServiceTicket(final Ticket ticket) {
        LOGGER.debug("UPDATING TICKET {}", ticket.getId());
        session.execute(this.updateStStmt.bind(serializer.serialize(ticket), ticket.getId()));
    }

    @Override
    public void addTicketToExpiryBucket(final Ticket ticket, final long expirationTime) {
        LOGGER.debug("adding to expiry bucket: Ticket: {}; expiry: {}", ticket.getId(), expirationTime / TEN_SECONDS);
        session.execute(this.insertExStmt.bind(expirationTime / TEN_SECONDS, ticket.getId()));
    }

    @Override
    public void removeRowFromTicketCleanerBucket(final long lastRun) {
        session.executeAsync(deleteExStmt.bind(lastRun));
    }

    @Override
    public List<TicketGrantingTicket> getExpiredTgts() {
        long lastRun = getLastRunTimestamp();
        final long currentTime = currentTimeBucket();
        if (lastRun == 0 || lastRun > currentTime) {
            updateLastRunTimestamp(currentTime);
        }

        LOGGER.debug("Searching for expired tickets. LastRun: {}; CurrentTime: {}", lastRun, currentTime);

        final List<TicketGrantingTicket> expiredTgts = new ArrayList<>();
        // TODO: replace maxTgtsToLoad with Java 8 stream
        while (lastRun < currentTime && expiredTgts.size() < maxTgtsToLoad) {
            expiredTgts.addAll(getExpiredTGTsIn(lastRun));
            removeRowFromTicketCleanerBucket(lastRun);
            updateLastRunTimestamp(++lastRun);
        }

        LOGGER.debug("Loaded {} possibly expired TGTs.", expiredTgts.size());

        return expiredTgts;
    }

    @Override
    public long getLastRunTimestamp() {
        final Row row = session.execute(this.selectLrStmt.bind()).one();
        return row == null ? System.currentTimeMillis() - maxTicketDuration : row.getLong("last_run");
    }

    @Override
    public void updateLastRunTimestamp(final long timestamp) {
        session.execute(this.updateLrStmt.bind(timestamp));
    }

    private List<TicketGrantingTicket> getExpiredTGTsIn(final long lastRunBucket) {
        return session.execute(this.selectExStmt.bind(lastRunBucket)).all()
                .stream()
                .map(row -> row.getString("id"))
                .map(this::getTicketGrantingTicket)
                .filter(Objects::nonNull)
                .filter(Ticket::isExpired)
                .collect(toList());
    }

    private static long currentTimeBucket() {
        return System.currentTimeMillis() / TEN_SECONDS;
    }
}
