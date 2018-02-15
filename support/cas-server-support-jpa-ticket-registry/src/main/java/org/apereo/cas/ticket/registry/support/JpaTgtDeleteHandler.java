package org.apereo.cas.ticket.registry.support;

import javax.persistence.EntityManager;

/**
 * Allows other support libraries to particpate in JPA TGT deletion (eg oauth).
 *
 * @author sbearcsiro
 * @since 5.2.3
 *
 */
public interface JpaTgtDeleteHandler {

    /**
     * Called when a single cascading ticket is being deleted.
     *
     * @param entityManager
     *            The entity manager
     * @param ticketId
     *            The TGT ticket id being deleted
     * @return the number of entities deleted
     */
    int deleteSingleTgt(EntityManager entityManager, String ticketId);
}
