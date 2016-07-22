package org.apereo.cas.support.events.dao;

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
    Collection<CasEvent> load();

    /**
     * Gets events of type.
     *
     * @param type the type
     * @return the events of type
     */
    Collection<CasEvent> getEventsOfType(String type);

    /**
     * Gets events for principal.
     *
     * @param id the id
     * @return the events for principal
     */
    Collection<CasEvent> getEventsForPrincipal(String id);
}
