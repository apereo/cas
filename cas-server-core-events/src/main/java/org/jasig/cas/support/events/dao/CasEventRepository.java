package org.jasig.cas.support.events.dao;

import java.util.Collection;

/**
 * Defines DAO operations over an events repository.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface CasEventRepository {
    /**
     * Save.
     *
     * @param event the event
     */
    void save(CasEventDTO event);

    /**
     * Load collection.
     *
     * @return the collection
     */
    Collection<CasEventDTO> load();

    /**
     * Gets events of type.
     *
     * @param type the type
     * @return the events of type
     */
    Collection<CasEventDTO> getEventsOfType(String type);
}
