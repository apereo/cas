package org.apereo.cas.support.events;

import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * This is {@link DynamoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class DynamoDbCasEventRepository extends AbstractCasEventRepository {
    private final DynamoDbCasEventsFacilitator dbCasEventsFacilitator;

    public DynamoDbCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                      final DynamoDbCasEventsFacilitator dbCasEventsFacilitator) {
        super(eventRepositoryFilter);
        this.dbCasEventsFacilitator = dbCasEventsFacilitator;
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        return dbCasEventsFacilitator.save(event);
    }

    @Override
    public Collection<? extends CasEvent> load() {
        return dbCasEventsFacilitator.getAll();
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type,
                                                                      final String principal) {
        return dbCasEventsFacilitator.getEventsOfTypeForPrincipal(type, principal);
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal,
                                                                      final ZonedDateTime dateTime) {
        return dbCasEventsFacilitator.getEventsOfTypeForPrincipal(type, principal, dateTime);
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type) {
        return dbCasEventsFacilitator.getEventsOfType(type);
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return dbCasEventsFacilitator.getEventsOfType(type, dateTime);
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id) {
        return dbCasEventsFacilitator.getEventsForPrincipal(id);
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return dbCasEventsFacilitator.getEventsForPrincipal(id, dateTime);
    }
}
