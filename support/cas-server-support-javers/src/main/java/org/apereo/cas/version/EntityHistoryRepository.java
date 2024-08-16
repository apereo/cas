package org.apereo.cas.version;

import java.util.List;

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
}
