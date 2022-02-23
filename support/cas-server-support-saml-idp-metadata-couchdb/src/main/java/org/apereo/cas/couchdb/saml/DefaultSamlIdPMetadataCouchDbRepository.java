package org.apereo.cas.couchdb.saml;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.util.Optional;

/**
 * This is {@link DefaultSamlIdPMetadataCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class DefaultSamlIdPMetadataCouchDbRepository extends CouchDbRepositorySupport<CouchDbSamlIdPMetadataDocument>
    implements SamlIdPMetadataCouchDbRepository {
    public DefaultSamlIdPMetadataCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbSamlIdPMetadataDocument.class, db, createIfNotExists);
    }

    @View(name = "all", map = "function(doc) { if (doc.metadata && doc.signingKey && doc.encryptionKey "
        + "&& (doc.appliesTo==null || doc.appliesTo=='CAS' || doc.appliesTo=='')) { emit(doc._id, doc) } }")
    @Override
    public CouchDbSamlIdPMetadataDocument getForAll() {
        val view = createQuery("all").limit(1);
        return db.queryView(view, CouchDbSamlIdPMetadataDocument.class).stream().findFirst().orElse(null);
    }

    @View(name = "service", map = "function(doc) { if (doc.metadata && doc.signingKey && doc.encryptionKey) { emit(doc._id, doc) } }")
    @Override
    public CouchDbSamlIdPMetadataDocument getForService(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isPresent()) {
            val view = createQuery("service").limit(1).queryParam("appliesTo", SamlIdPMetadataGenerator.getAppliesToFor(registeredService));
            return db.queryView(view, CouchDbSamlIdPMetadataDocument.class).stream().findFirst().orElse(null);
        }
        return getForAll();
    }

    @Override
    public void initialize() {
        initStandardDesignDocument();
    }
}
