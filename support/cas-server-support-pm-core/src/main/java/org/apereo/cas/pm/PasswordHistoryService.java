package org.apereo.cas.pm;

import org.apereo.cas.pm.impl.history.PasswordHistoryEntity;

import org.springframework.core.Ordered;

import java.util.Collection;

/**
 * This is {@link PasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface PasswordHistoryService extends Ordered {

    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Determine whether password request
     * can be accepted based on history requirements and tracking.
     *
     * @param changeRequest the change request
     * @return true/false
     */
    boolean exists(PasswordChangeRequest changeRequest);

    /**
     * Store password request in history.
     *
     * @param changeRequest the change request
     * @return true/false
     */
    boolean store(PasswordChangeRequest changeRequest);

    /**
     * Fetch all collection.
     *
     * @return the collection
     */
    Collection<? extends PasswordHistoryEntity> fetchAll();

    /**
     * Fetch collection.
     *
     * @param username the username
     * @return the collection
     */
    Collection<? extends PasswordHistoryEntity> fetch(String username);

    /**
     * Remove.
     *
     * @param username the username
     */
    void remove(String username);

    /**
     * Remove all.
     */
    void removeAll();
}
