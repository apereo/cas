package org.apereo.cas.couchdb.core;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

/**
 * This is {@link ProfileCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class ProfileCouchDbRepository extends CouchDbRepositorySupport<CouchDbProfileDocument> {
    public ProfileCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbProfileDocument.class, db, "pac4j", createIfNotExists);
    }

    /**
     * Find profile by username.
     * @param username to be searched for
     * @return profile found
     */
    @View(name = "by_username", map = "function(doc) { if(doc.username){ emit(doc.username, doc) } }")
    public CouchDbProfileDocument findByUsername(final String username) {
        return queryView("by_username", username).stream().findFirst().orElse(null);
    }

    /**
     * Find profile by linkedid.
     * @param linkedid to be searched for
     * @return profile found
     */
    @View(name = "by_linkedid", map = "function(doc) { if(doc.linkedid){ emit(doc.linkedid, doc) } }")
    public CouchDbProfileDocument findByLinkedid(final String linkedid) {
        return queryView("by_linkedid", linkedid).stream().findFirst().orElse(null);
    }
}
