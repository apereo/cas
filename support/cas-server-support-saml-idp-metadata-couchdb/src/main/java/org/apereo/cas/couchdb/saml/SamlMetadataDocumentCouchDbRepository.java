package org.apereo.cas.couchdb.saml;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

/**
 * This is {@link SamlMetadataDocumentCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if(doc.name && doc.value) { emit(doc._id, doc) } }")
public class SamlMetadataDocumentCouchDbRepository extends CouchDbRepositorySupport<CouchDbSamlMetadataDocument> {
    public SamlMetadataDocumentCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbSamlMetadataDocument.class, db, createIfNotExists);
    }

    /**
     * Find by name.
     * @param name name to search for
     * @return SAML metadata
     */
    @View(name = "by_name", map = "function(doc) { if(doc.name && doc.value) { emit(doc.name, doc) } }")
    public CouchDbSamlMetadataDocument findFirstByName(final String name) {
        val view = createQuery("by_name").key(name).limit(1);
        return db.queryView(view, CouchDbSamlMetadataDocument.class).stream().findFirst().orElse(null);
    }
}
