package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractCasEventRepository implements CasEventRepository {

    /**
     * Field name to track event type.
     */
    protected static final String TYPE_PARAM = "type";

    /**
     * Field name to track creation type of event.
     */
    protected static final String CREATION_TIME_PARAM = "creationTime";

    /**
     * Field name to track principal linked to the event.
     */
    protected static final String PRINCIPAL_ID_PARAM = "principalId";

    private final CasEventRepositoryFilter eventRepositoryFilter;

    private static ZonedDateTime convertEventCreationTime(final CasEvent event) {
        return DateTimeUtils.convertToZonedDateTime(event.getCreationTime());
    }

    @Override
    public void save(final CasEvent event) {
        if (getEventRepositoryFilter().shouldSaveEvent(event)) {
            saveInternal(event);
        }
    }

    @Override
    public Collection<? extends CasEvent> load(final ZonedDateTime dateTime) {
        return load()
            .stream()
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dateTime) || dt.isAfter(dateTime);
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        return getEventsForPrincipal(principal)
            .stream()
            .filter(event -> event.getType().equals(type))
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        return getEventsOfTypeForPrincipal(type, principal)
            .stream()
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dateTime) || dt.isAfter(dateTime);
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type) {
        val events = load();
        return events.stream().filter(event -> event.getType().equals(type)).collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return getEventsOfType(type)
            .stream()
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dt) || dt.isAfter(dt);
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id) {
        return load()
            .stream()
            .filter(e -> e.getPrincipalId().equalsIgnoreCase(id))
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return getEventsForPrincipal(id)
            .stream()
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dateTime) || dt.isAfter(dateTime);
            })
            .collect(Collectors.toSet());
    }

    public abstract void saveInternal(CasEvent event);
}
