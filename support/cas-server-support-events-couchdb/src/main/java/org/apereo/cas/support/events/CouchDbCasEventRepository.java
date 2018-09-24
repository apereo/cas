package org.apereo.cas.support.events;

import org.apereo.cas.couchdb.events.CouchDbCasEvent;
import org.apereo.cas.couchdb.events.EventCouchDbRepository;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbCasEventRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class CouchDbCasEventRepository extends AbstractCasEventRepository {

    private final EventCouchDbRepository couchDb;

    private final boolean asynchronous;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Collection<CasEvent> castEvents(final Collection<CouchDbCasEvent> events) {
        return events.stream().map(event -> (CasEvent) event).collect(Collectors.toSet());
    }

    @Override
    public void save(final CasEvent event) {
        if (asynchronous) {
            this.executorService.execute(() -> couchDb.add(new CouchDbCasEvent(event)));
        } else {
            couchDb.add(new CouchDbCasEvent(event));
        }
    }

    @Override
    public Collection<? extends CasEvent> load() {
        return couchDb.getAll();
    }

    @Override
    public Collection<CasEvent> getEventsOfType(final String type) {
        return castEvents(couchDb.findByType(type));
    }

    @Override
    public Collection<CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return castEvents(couchDb.findByTypeSince(type, dateTime.toLocalDateTime()));
    }

    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        return castEvents(couchDb.findByTypeForPrincipalId(type, principal));
    }

    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        return castEvents(couchDb.findByTypeForPrincipalSince(type, principal, dateTime.toLocalDateTime()));
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return castEvents(couchDb.findByPrincipalSince(id, dateTime.toLocalDateTime()));
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id) {
        return castEvents(couchDb.findByPrincipalId(id));
    }
}
