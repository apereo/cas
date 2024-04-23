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
    /**
     * Default bean name.
     */
    String BEAN_NAME = "passwordHistoryService";

    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Determine whether password request
     * can be accepted based on history requirements and tracking.
     *
     * @param changeRequest the change request
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean exists(PasswordChangeRequest changeRequest) throws Throwable;

    /**
     * Store password request in history.
     *
     * @param changeRequest the change request
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean store(PasswordChangeRequest changeRequest) throws Throwable;

    /**
     * Fetch all collection.
     *
     * @return the collection
     * @throws Throwable the throwable
     */
    Collection<? extends PasswordHistoryEntity> fetchAll() throws Throwable;

    /**
     * Fetch collection.
     *
     * @param username the username
     * @return the collection
     * @throws Throwable the throwable
     */
    Collection<? extends PasswordHistoryEntity> fetch(String username) throws Throwable;

    /**
     * Remove.
     *
     * @param username the username
     * @throws Throwable the throwable
     */
    void remove(String username) throws Throwable;

    /**
     * Remove all.
     *
     * @throws Throwable the throwable
     */
    void removeAll() throws Throwable;
}
