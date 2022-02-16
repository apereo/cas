package org.apereo.cas.couchdb.core;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

/**
 * This is {@link DefaultProfileCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class DefaultProfileCouchDbRepository extends CouchDbRepositorySupport<CouchDbProfileDocument>
    implements ProfileCouchDbRepository<CouchDbProfileDocument> {
    public DefaultProfileCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbProfileDocument.class, db, "pac4j", createIfNotExists);
    }

    @Override
    @View(name = "by_username", map = "function(doc) { if(doc.username){ emit(doc.username, doc) } }")
    public CouchDbProfileDocument findByUsername(final String username) {
        return queryView("by_username", username).stream().findFirst().orElse(null);
    }

    @Override
    @View(name = "by_linkedid", map = "function(doc) { if(doc.linkedid){ emit(doc.linkedid, doc) } }")
    public CouchDbProfileDocument findByLinkedId(final String linkedid) {
        return queryView("by_linkedid", linkedid).stream().findFirst().orElse(null);
    }

    @Override
    public void initialize() {
        initStandardDesignDocument();
    }
}
