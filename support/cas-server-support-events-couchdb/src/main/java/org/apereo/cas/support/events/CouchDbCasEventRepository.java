package org.apereo.cas.support.events;

import org.apereo.cas.couchdb.events.CouchDbCasEvent;
import org.apereo.cas.couchdb.events.EventCouchDbRepository;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.val;
import org.springframework.beans.factory.DisposableBean;

import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * This is {@link CouchDbCasEventRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbCasEventRepository extends AbstractCasEventRepository implements DisposableBean {

    private final EventCouchDbRepository couchDb;

    private final boolean asynchronous;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        r -> new Thread(r, "CouchDbCasEventRepositoryThread"));

    public CouchDbCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                     final EventCouchDbRepository couchDb, final boolean asynchronous) {
        super(eventRepositoryFilter);
        this.couchDb = couchDb;
        this.asynchronous = asynchronous;
    }

    private static Stream<? extends CasEvent> castEvents(final Stream<? extends CouchDbCasEvent> events) {
        return events;
    }

    @Override
    public Stream<? extends CasEvent> load() {
        return couchDb.getAll().stream();
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        return castEvents(couchDb.findByTypeForPrincipalId(type, principal));
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal,
                                                                  final ZonedDateTime dateTime) {
        return castEvents(couchDb.findByTypeForPrincipalSince(type, principal, dateTime));
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type) {
        return castEvents(couchDb.findByType(type));
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return castEvents(couchDb.findByTypeSince(type, dateTime));
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id) {
        return castEvents(couchDb.findByPrincipalId(id));
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return castEvents(couchDb.findByPrincipalSince(id, dateTime));
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        val cdbEvent = new CouchDbCasEvent(event);
        if (asynchronous) {
            this.executorService.execute(() -> couchDb.add(cdbEvent));
        } else {
            couchDb.add(cdbEvent);
        }
        return cdbEvent;
    }

    @Override
    public void destroy() {
        this.executorService.shutdown();
    }
}
