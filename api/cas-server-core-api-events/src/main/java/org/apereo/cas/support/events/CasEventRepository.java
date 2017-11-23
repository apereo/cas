package org.apereo.cas.support.events;

import org.apereo.cas.support.events.dao.CasEvent;

import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Defines DAO operations over an events repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasEventRepository {
    /**
     * Save.
     *
     * @param event the event
     */
    void save(CasEvent event);

    /**
     * Load collection.
     *
     * @return the collection
     */
    Collection<? extends CasEvent> load();

    /**
     * Load collection of events created after the given date.
     *
     * @param dateTime the date time
     * @return the collection
     */
    Collection<CasEvent> load(ZonedDateTime dateTime);

    /**
     * Gets events of type for principal.
     *
     * @param type      the type
     * @param principal the principal
     * @return the events of type
     */
    Collection<CasEvent> getEventsOfTypeForPrincipal(String type, String principal);

    /**
     * Gets events of type for principal after date.
     *
     * @param type      the type
     * @param principal the principal
     * @param dateTime  the date time
     * @return the events of type
     */
    Collection<CasEvent> getEventsOfTypeForPrincipal(String type, String principal, ZonedDateTime dateTime);

    /**
     * Gets events of type.
     *
     * @param type the type
     * @return the events of type
     */
    Collection<CasEvent> getEventsOfType(String type);

    /**
     * Gets events of type after date.
     *
     * @param type     the type
     * @param dateTime the date time
     * @return the events of type
     */
    Collection<CasEvent> getEventsOfType(String type, ZonedDateTime dateTime);

    /**
     * Gets events for principal.
     *
     * @param id the id
     * @return the events for principal
     */
    Collection<CasEvent> getEventsForPrincipal(String id);

    /**
     * Gets events for principal after date.
     *
     * @param id       the id
     * @param dateTime the date time
     * @return the events for principal
     */
    Collection<CasEvent> getEventsForPrincipal(String id, ZonedDateTime dateTime);
}
