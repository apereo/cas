package org.apereo.cas.couchdb.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.impl.ObjectMapperFactory;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.jooq.lambda.Unchecked;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is {@link EventCouchDbRepository}. Typed interface to CouchDB.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { emit(doc._id, doc) }")
public class EventCouchDbRepository extends CouchDbRepositorySupport<CouchDbCasEvent> {
    private final ObjectMapper objectMapper;

    public EventCouchDbRepository(final CouchDbConnector db,
                                  final boolean createIfNotExists,
                                  final ObjectMapperFactory objectMapperFactory) {
        super(CouchDbCasEvent.class, db, createIfNotExists);
        this.objectMapper = objectMapperFactory.createObjectMapper(db);
    }

    /**
     * Find by event type.
     *
     * @param type event type
     * @return events of requested type
     */
    @GenerateView
    public Stream<CouchDbCasEvent> findByType(final String type) {
        val query = createQuery("by_type").includeDocs(true).key(type);
        return StreamSupport.stream(db.queryForStreamingView(query).spliterator(), false)
            .map(Unchecked.function(row -> objectMapper.readValue(row.getDoc(), CouchDbCasEvent.class)));
    }

    /**
     * Fund by type since a given date.
     *
     * @param type type to search for
     * @param dt   time to search since
     * @return events of the given type since the given time
     */
    public Stream<CouchDbCasEvent> findByTypeSince(final String type, final ZonedDateTime dt) {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return findByType(type)
            .filter(event -> {
                val eventDate = ZonedDateTime.parse(event.getCreationTime());
                return eventDate.isEqual(dt) || (eventDate.isAfter(dt) && eventDate.isBefore(now));
            });
    }

    /**
     * Find by type and principal id.
     *
     * @param type        event type
     * @param principalId principal to search for
     * @return events of requested type and principal
     */
    @View(name = "by_type_for_principal_id", map = "function(doc) { emit([doc.type, doc.principalId], doc) }")
    public Stream<CouchDbCasEvent> findByTypeForPrincipalId(final String type, final String principalId) {
        val query = createQuery("by_type_for_principal_id").includeDocs(true).key(ComplexKey.of(type, principalId));
        return StreamSupport.stream(db.queryForStreamingView(query).spliterator(), false)
            .map(Unchecked.function(row -> objectMapper.readValue(row.getDoc(), CouchDbCasEvent.class)));

    }

    /**
     * Find by type and principal id.
     *
     * @param type        event type
     * @param principalId principal to search for
     * @param dt          time to search after
     * @return events of requested type and principal since given time
     */
    public Stream<CouchDbCasEvent> findByTypeForPrincipalSince(final String type,
                                                               final String principalId,
                                                               final ZonedDateTime dt) {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return findByTypeForPrincipalId(type, principalId)
            .filter(event -> {
                val eventDate = ZonedDateTime.parse(event.getCreationTime());
                return eventDate.isEqual(dt) || (eventDate.isAfter(dt) && eventDate.isBefore(now));
            });
    }

    /**
     * Find by principal.
     *
     * @param principalId principal to search for
     * @return events for the given principal
     */
    @GenerateView
    public Stream<CouchDbCasEvent> findByPrincipalId(final String principalId) {
        val query = createQuery("by_principalId").includeDocs(true).key(principalId);
        return StreamSupport.stream(db.queryForStreamingView(query).spliterator(), false)
            .map(Unchecked.function(row -> objectMapper.readValue(row.getDoc(), CouchDbCasEvent.class)));
    }

    /**
     * Find by principal.
     *
     * @param principalId  principal to search for
     * @param creationTime time to search after
     * @return events for the given principal after the given time
     */
    public Stream<? extends CouchDbCasEvent> findByPrincipalSince(final String principalId,
                                                                  final ZonedDateTime creationTime) {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return findByPrincipalId(principalId)
            .filter(event -> {
                val eventDate = ZonedDateTime.parse(event.getCreationTime());
                return eventDate.isEqual(creationTime)
                       || (eventDate.isAfter(creationTime) && eventDate.isBefore(now));
            });
    }
}
