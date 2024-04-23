package org.apereo.cas.support.events;

import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.function.FunctionUtils;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

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
    public void removeAll() {
        FunctionUtils.doUnchecked(__ -> dbCasEventsFacilitator.createTable(true));
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) throws Exception {
        return dbCasEventsFacilitator.save(event);
    }

    @Override
    public Stream<? extends CasEvent> load() {
        return dbCasEventsFacilitator.getAll();
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type,
                                                                      final String principal) {
        return dbCasEventsFacilitator.getEventsOfTypeForPrincipal(type, principal);
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal,
                                                                      final ZonedDateTime dateTime) {
        return dbCasEventsFacilitator.getEventsOfTypeForPrincipal(type, principal, dateTime);
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type) {
        return dbCasEventsFacilitator.getEventsOfType(type);
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return dbCasEventsFacilitator.getEventsOfType(type, dateTime);
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id) {
        return dbCasEventsFacilitator.getEventsForPrincipal(id);
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return dbCasEventsFacilitator.getEventsForPrincipal(id, dateTime);
    }
}
