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
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public class CassandraTicketRegistry<T> extends AbstractTicketRegistry implements CassandraTicketRegistryDao {

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
                                   final TicketSerializer<T> serializer, final Class<T> typeToWriteToCassandra, final String tgtTable, final String stTable,
                                   final String expiryTable, final String lastRunTable) {
        this.ticketCatalog = ticketCatalog;
        this.serializer = serializer;
        this.typeToWriteToCassandra = typeToWriteToCassandra;
        final Cluster cluster = Cluster.builder().addContactPoints(contactPoints.split(",")).withCredentials(username, password)
                .withProtocolVersion(ProtocolVersion.V3).build();

        this.session = cluster.connect();

        this.selectTgtStmt = session.prepare("select ticket from " + tgtTable + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.insertTgtStmt = session.prepare("insert into " + tgtTable + " (id, ticket, ticket_granting_ticket_id, expiration_bucket) values (?, ?, ?, ?) ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.deleteTgtStmt = session.prepare("delete from " + tgtTable + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.updateTgtStmt = session.prepare("update " + tgtTable + " set ticket = ? where id = ? ").setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        this.selectStStmt = session.prepare("select ticket from " + stTable + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.insertStStmt = session.prepare("insert into " + stTable + " (id, ticket) values (?, ?) ").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.deleteStStmt = session.prepare("delete from " + stTable + " where id = ?").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        this.updateStStmt = session.prepare("update " + stTable + " set ticket = ? where id = ? ").setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        this.selectExStmt = session.prepare("select ticket from " + expiryTable + " where expiration_bucket = ? ")
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.selectDateExStmt = session.prepare("select expiration_bucket from " + expiryTable)
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
    public void addTicket(final Ticket ticket) {
        final String ticketId = ticket.getId();
        LOGGER.debug("Inserting ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final String storageName = ticketDefinition.getProperties().getStorageName();

        if ("ticketGrantingTicket".equals(storageName)) {
            addTicketGrantingTicket(ticket);
        } else if ("serviceTicket".equals(storageName)) {
            addServiceTicket(ticket);
        } else {
            LOGGER.error("Inserting unknown ticket type {}", ticket.getClass().getName());
        }
    }

    @Override
    public int deleteTicket(final String id) {
        if (deleteSingleTicket(id)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public long deleteAll() {
        return 0;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        //        Function<String, Boolean> deleteTgt = this::deleteTicketGrantingTicket;
        //        Function<String, Boolean> deleteSt = this::deleteServiceTicket;
        //
        //        return applyForGivenType(ticketId, deleteTgt, deleteSt);

        LOGGER.debug("Deleting ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final String storageName = ticketDefinition.getProperties().getStorageName();
        if ("ticketGrantingTicket".equals(storageName)) {
            return deleteTicketGrantingTicket(ticketId);
        } else if ("serviceTickets".equals(storageName)) {
            return deleteServiceTicket(ticketId);
        } else {
            LOGGER.error("Deleting unknown ticket type {}", ticketId);
            return false;
        }
    }

    //    private boolean applyForGivenType(final String ticketId, final Function<String, Boolean> tgtAction, final Function<String, Boolean> stAction) {
    //        LOGGER.debug("Deleting ticket {}", ticketId);
    //        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
    //        final String storageName = ticketDefinition.getProperties().getStorageName();
    //        if (storageName.equals("ticketGrantingTicket")) {
    //            return tgtAction.apply(ticketId);
    //        } else if (storageName.equals("serviceTickets")) {
    //            return stAction.apply(ticketId);
    //        } else {
    //            LOGGER.error("Deleting unknown ticket type {}", ticketId);
    //            return false;
    //        }
    //    }

    @Override
    public Ticket getTicket(final String ticketId) {
        LOGGER.debug("Querying ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final String storageName = ticketDefinition.getProperties().getStorageName();
        if ("ticketGrantingTicket".equals(storageName)) {
            return getTicketGrantingTicket(ticketId);
        } else if ("serviceTickets".equals(storageName)) {
            return getServiceTicket(ticketId);
        } else {
            LOGGER.error("Requesting unknown ticket type {}", ticketId);
            return null;
        }
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        final String ticketId = ticket.getId();
        LOGGER.debug("Updating ticket {}", ticketId);
        final TicketDefinition ticketDefinition = ticketCatalog.find(ticketId);
        final String storageName = ticketDefinition.getProperties().getStorageName();
        if ("ticketGrantingTicket".equals(storageName)) {
            updateTicketGrantingTicket(ticket);
        } else if ("serviceTickets".equals(storageName)) {
            updateServiceTicket(ticket);
        } else {
            LOGGER.error("Updating unknown ticket type {}", ticket.getClass().getName());
        }
        return ticket;
    }

    @Override
    public Stream<TicketGrantingTicket> getExpiredTgts() {
        final long lastRun = getLastRunTimestamp();
        final long currentTime = currentTimeBucket();

        LOGGER.debug("Searching for expired tickets. LastRun: {}; CurrentTime: {}", lastRun, currentTime);

        return LongStream.rangeClosed(lastRun, currentTime)
                .mapToObj(time -> {
                    updateLastRunTimestamp(time);
                    return getExpiredTGTsIn(time);
                })
                .flatMap(Function.identity());
    }

    @Override
    public Collection<Ticket> getTickets() {
        return null;
    }

    @Override
    public boolean deleteTicketGrantingTicket(final String id) {
        LOGGER.debug("Deleting ticket {}", id);
        return session.execute(this.deleteTgtStmt.bind(id)).wasApplied();
    }

    private void addTicketGrantingTicket(final Ticket ticket) {
        LOGGER.debug("Inserting ticket {}", ticket.getId());
        final String parentTgtId = ticket.getGrantingTicket() == null ? null : ticket.getGrantingTicket().getId();
        final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
        session.execute(this.insertTgtStmt.bind(ticket.getId(), serializer.serializeTGT(ticket), parentTgtId, calculateExpirationDate(tgt) / TEN));
    }

    private void addServiceTicket(final Ticket ticket) {
        LOGGER.debug("Inserting ticket {}", ticket.getId());
        session.execute(this.insertStStmt.bind(ticket.getId(), serializer.serializeST(ticket)));
    }

    private boolean deleteServiceTicket(final String id) {
        LOGGER.debug("Deleting ticket {}", id);
        session.executeAsync(this.deleteStStmt.bind(id));
        return true;
    }

    private TicketGrantingTicket getTicketGrantingTicket(final String id) {
        LOGGER.debug("Reading ticket {}", id);
        final Row row = session.execute(this.selectTgtStmt.bind(id)).one();
        if (row == null) {
            LOGGER.info("ticket {} not found", id);
            return null;
        }
        return serializer.deserializeTGT(row.get(FIRST_COLUMN_INDEX, typeToWriteToCassandra));
    }

    private Ticket getServiceTicket(final String id) {
        LOGGER.debug("Reading ticket {}", id);
        final Row row = session.execute(this.selectStStmt.bind(id)).one();
        if (row == null) {
            LOGGER.info("ticket {} not found", id);
            return null;
        }
        return serializer.deserializeST(row.get(FIRST_COLUMN_INDEX, typeToWriteToCassandra));
    }

    private void updateTicketGrantingTicket(final Ticket ticket) {
        final TicketGrantingTicketImpl tgt = (TicketGrantingTicketImpl) ticket;
        session.execute(this.updateTgtStmt.bind(serializer.serializeTGT(ticket), ticket.getId(), calculateExpirationDate(tgt) / TEN));
    }

    private void updateServiceTicket(final Ticket ticket) {
        session.execute(this.updateStStmt.bind(serializer.serializeST(ticket), ticket.getId()));
    }

    private Stream<TicketGrantingTicket> getExpiredTGTsIn(final long lastRunBucket) {
        final ResultSet resultSet = session.execute(this.selectExStmt.bind(lastRunBucket));

        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> serializer.deserializeTGT(row.get(FIRST_COLUMN_INDEX, typeToWriteToCassandra)))
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
