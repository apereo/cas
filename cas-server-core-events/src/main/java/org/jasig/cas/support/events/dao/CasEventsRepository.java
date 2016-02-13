package org.jasig.cas.support.events.dao;

import org.jasig.cas.support.events.AbstractCasEvent;

import java.util.Collection;

/**
 * Defines DAO operations over an events repository.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface CasEventsRepository {
    /**
     * Save.
     *
     * @param event the event
     */
    void save(AbstractCasEvent event);

    /**
     * Load collection.
     *
     * @return the collection
     */
    Collection<AbstractCasEvent> load();

}
