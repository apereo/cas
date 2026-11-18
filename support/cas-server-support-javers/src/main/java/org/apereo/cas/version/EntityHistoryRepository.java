package org.apereo.cas.version;

import module java.base;

/**
 * This is {@link EntityHistoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface EntityHistoryRepository {
    /**
     * Save object.
     *
     * @param object the result
     * @return the saved object
     */
    Object save(Object object);

    /**
     * Gets history.
     *
     * @param object the registered service
     * @return the history
     */
    List<HistoricalEntity> getHistory(Object object);

    /**
     * Format changes as string.
     *
     * @param object the instance
     * @return the string
     */
    String getChangelog(Object object);

    /**
     * Record that an entity was deleted, so its history ends with the deletion.
     *
     * @param object the deleted entity
     */
    default void delete(final Object object) {
    }

    /**
     * The latest changes to any entity, newest first, as recorded in the shared history.
     *
     * @param limit how many changes to return at most
     * @return the changes
     */
    default List<EntityChange> getRecentChanges(final int limit) {
        return List.of();
    }
}
