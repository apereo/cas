package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractCasEventRepository implements CasEventRepository {

    /**
     * The constant LOGGER.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractCasEventRepository.class);
    /**
     * The constant TYPE_PARAM.
     */
    protected static final String TYPE_PARAM = "type";
    /**
     * The constant CREATION_TIME_PARAM.
     */
    protected static final String CREATION_TIME_PARAM = "creationTime";
    /**
     * The constant PRINCIPAL_ID_PARAM.
     */
    protected static final String PRINCIPAL_ID_PARAM = "principalId";

    @Override
    public Collection<CasEvent> getEventsOfType(final String type) {
        final Collection<? extends CasEvent> events = load();
        return events.stream().filter(event -> event.getType().equals(type)).collect(Collectors.toSet());
    }

    @Override
    public Collection<CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return getEventsOfType(type)
                .stream()
                .filter(e -> e.getCreationTime().isEqual(dateTime) || e.getCreationTime().isAfter(dateTime))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        return getEventsForPrincipal(principal)
                .stream()
                .filter(event -> event.getType().equals(type))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        return getEventsOfTypeForPrincipal(type, principal)
                .stream()
                .filter(e -> e.getCreationTime().isEqual(dateTime) || e.getCreationTime().isAfter(dateTime))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<CasEvent> load(final ZonedDateTime dateTime) {
        return load().stream()
                .filter(e -> e.getCreationTime().isEqual(dateTime) || e.getCreationTime().isAfter(dateTime))
                .collect(Collectors.toSet());
    }
    
    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return getEventsForPrincipal(id)
                .stream()
                .filter(e -> e.getCreationTime().isEqual(dateTime) || e.getCreationTime().isAfter(dateTime))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id) {
        return load().stream().filter(e -> e.getPrincipalId().equalsIgnoreCase(id)).collect(Collectors.toSet());
    }

}
