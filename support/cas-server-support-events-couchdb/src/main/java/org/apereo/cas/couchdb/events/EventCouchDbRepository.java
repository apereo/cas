package org.apereo.cas.couchdb.events;

import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link EventCouchDbRepository}. Typed interface to CouchDB.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { emit(doc._id, doc) }")
public class EventCouchDbRepository extends CouchDbRepositorySupport<CouchDbCasEvent> {
    public EventCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbCasEvent.class, db, createIfNotExists);
    }

    /**
     * Find by event type.
     *
     * @param type event type
     * @return events of requested type
     */
    @GenerateView
    public List<CouchDbCasEvent> findByType(final String type) {
        return queryView("by_type", type);
    }

    /**
     * Fund by type since a given date.
     *
     * @param type type to search for
     * @param dt   time to search since
     * @return events of the given type since the given time
     */
    public List<CouchDbCasEvent> findByTypeSince(final String type, final ZonedDateTime dt) {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return findByType(type)
            .stream()
            .filter(event -> {
                val eventDate = ZonedDateTime.parse(event.getCreationTime());
                return eventDate.isEqual(dt) || (eventDate.isAfter(dt) && eventDate.isBefore(now));
            })
            .collect(Collectors.toList());
    }

    /**
     * Find by type and principal id.
     *
     * @param type        event type
     * @param principalId principal to search for
     * @return events of requested type and principal
     */
    @View(name = "by_type_for_principal_id", map = "function(doc) { emit([doc.type, doc.principalId], doc) }")
    public Collection<CouchDbCasEvent> findByTypeForPrincipalId(final String type, final String principalId) {
        val view = createQuery("by_type_for_principal_id").key(ComplexKey.of(type, principalId));
        return db.queryView(view, CouchDbCasEvent.class);
    }

    /**
     * Find by type and principal id.
     *
     * @param type        event type
     * @param principalId principal to search for
     * @param dt          time to search after
     * @return events of requested type and principal since given time
     */
    public Collection<CouchDbCasEvent> findByTypeForPrincipalSince(final String type,
                                                                   final String principalId,
                                                                   final ZonedDateTime dt) {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return findByTypeForPrincipalId(type, principalId)
            .stream()
            .filter(event -> {
                val eventDate = ZonedDateTime.parse(event.getCreationTime());
                return eventDate.isEqual(dt) || (eventDate.isAfter(dt) && eventDate.isBefore(now));
            })
            .collect(Collectors.toList());
    }

    /**
     * Find by principal.
     *
     * @param principalId principal to search for
     * @return events for the given principal
     */
    @GenerateView
    public Collection<CouchDbCasEvent> findByPrincipalId(final String principalId) {
        return queryView("by_principalId", principalId);
    }

    /**
     * Find by principal.
     *
     * @param principalId  principal to search for
     * @param creationTime time to search after
     * @return events for the given principal after the given time
     */
    public Collection<CouchDbCasEvent> findByPrincipalSince(final String principalId, final ZonedDateTime creationTime) {

        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return findByPrincipalId(principalId)
            .stream()
            .filter(event -> {
                val eventDate = ZonedDateTime.parse(event.getCreationTime());
                return eventDate.isEqual(creationTime) || (eventDate.isAfter(creationTime) && eventDate.isBefore(now));
            })
            .collect(Collectors.toList());
    }
}
