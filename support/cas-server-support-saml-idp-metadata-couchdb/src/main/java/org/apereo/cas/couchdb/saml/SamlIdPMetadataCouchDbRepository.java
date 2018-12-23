package org.apereo.cas.couchdb.saml;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

/**
 * This is {@link SamlIdPMetadataCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if (doc.metadata || doc.signingKey || doc.encryptionKey) { emit(doc._id, doc) } }")
public class SamlIdPMetadataCouchDbRepository extends CouchDbRepositorySupport<CouchDbSamlIdPMetadataDocument> {
    public SamlIdPMetadataCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbSamlIdPMetadataDocument.class, db, createIfNotExists);
    }

    /**
     * Get one SAML metadata document.
     * @return first found SAML metatdata doc
     */
    public CouchDbSamlIdPMetadataDocument getOne() {
        val view = createQuery("all").limit(1);
        return db.queryView(view, CouchDbSamlIdPMetadataDocument.class).stream().findFirst().orElse(null);
    }
}
