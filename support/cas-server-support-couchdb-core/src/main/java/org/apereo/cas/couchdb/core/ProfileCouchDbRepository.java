package org.apereo.cas.couchdb.core;

import org.ektorp.support.GenericRepository;

/**
 * This is {@link ProfileCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public interface ProfileCouchDbRepository<T> extends GenericRepository<T> {
    /**
     * Find by username.
     *
     * @param username the username
     * @return the couch db profile document
     */
    CouchDbProfileDocument findByUsername(String username);

    /**
     * Find by linked id.
     *
     * @param linkedId the linked id
     * @return the couch db profile document
     */
    CouchDbProfileDocument findByLinkedId(String linkedId);

    /**
     * Initialize.
     */
    void initialize();
}
