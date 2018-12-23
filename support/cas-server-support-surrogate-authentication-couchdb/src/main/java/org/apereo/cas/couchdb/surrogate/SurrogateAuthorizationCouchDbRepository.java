package org.apereo.cas.couchdb.surrogate;

import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.util.List;

/**
 * This is {@link SurrogateAuthorizationCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.surrogate && doc.principal) { emit(doc._id, doc) } }")
public class SurrogateAuthorizationCouchDbRepository extends CouchDbRepositorySupport<CouchDbSurrogateAuthorization> {
    public SurrogateAuthorizationCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbSurrogateAuthorization.class, db, createIfNotExists);
    }

    /**
     * Find by surrogate.
     * @param surrogate username to retrieve authorized principals
     * @return list of surrogate documents
     */
    @View(name = "by_surrogate", map = "function(doc) { if(doc.surrogate && doc.principal) { emit(doc.principal, doc.surrogate) } }")
    public List<String> findByPrincipal(final String surrogate) {
        val view = createQuery("by_surrogate").key(surrogate);
        return db.queryView(view, String.class);
    }

    /**
     * Find by surrogate, principal, service touple for authorization check.
     * @param surrogate Surrogate user to validate access.
     * @param principal Principal to validate the surrogate can access.
     * @return Surrogate/principal if authorized
     */
    @View(name = "by_surrogate_principal", map = "function(doc) { if(doc.surrogate && doc.principal) { emit([doc.principal, doc.surrogate], doc) } }")
    public List<CouchDbSurrogateAuthorization> findBySurrogatePrincipal(final String surrogate, final String principal) {
        return queryView("by_surrogate_principal", ComplexKey.of(principal, surrogate));
    }
}
