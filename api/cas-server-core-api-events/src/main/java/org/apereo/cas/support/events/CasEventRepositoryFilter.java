package org.apereo.cas.support.events;

import org.apereo.cas.support.events.dao.CasEvent;

/**
 * This is {@link CasEventRepositoryFilter}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface CasEventRepositoryFilter {

    /**
     * No op cas event repository filter.
     *
     * @return the cas event repository filter
     */
    static CasEventRepositoryFilter noOp() {
        return new NoOpCasEventRepositoryFilter();
    }

    /**
     * Whether this event can be saved/tracked by CAS event repository.
     *
     * @param event the event
     * @return true/false
     */
    default boolean shouldSaveEvent(CasEvent event) {
        return true;
    }

    /**
     * The type No op cas event repository filter.
     */
    class NoOpCasEventRepositoryFilter implements CasEventRepositoryFilter {

    }
}
