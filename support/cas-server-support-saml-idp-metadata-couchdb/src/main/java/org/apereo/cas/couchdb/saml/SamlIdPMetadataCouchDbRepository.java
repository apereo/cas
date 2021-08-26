package org.apereo.cas.couchdb.saml;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.util.Optional;

/**
 * This is {@link SamlIdPMetadataCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class SamlIdPMetadataCouchDbRepository extends CouchDbRepositorySupport<CouchDbSamlIdPMetadataDocument> {
    public SamlIdPMetadataCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbSamlIdPMetadataDocument.class, db, createIfNotExists);
    }

    /**
     * Get one SAML metadata document.
     *
     * @return first found SAML metadata doc
     */
    @View(name = "all", map = "function(doc) { if (doc.metadata && doc.signingKey && doc.encryptionKey "
        + "&& (doc.appliesTo==null || doc.appliesTo=='CAS' || doc.appliesTo=='')) { emit(doc._id, doc) } }")
    public CouchDbSamlIdPMetadataDocument getForAll() {
        val view = createQuery("all").limit(1);
        return db.queryView(view, CouchDbSamlIdPMetadataDocument.class).stream().findFirst().orElse(null);
    }

    /**
     * Gets for service.
     *
     * @param registeredService the registered service
     * @return the for service
     */
    @View(name = "service", map = "function(doc) { if (doc.metadata && doc.signingKey && doc.encryptionKey) { emit(doc._id, doc) } }")
    public CouchDbSamlIdPMetadataDocument getForService(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isPresent()) {
            val view = createQuery("service").limit(1).queryParam("appliesTo", SamlIdPMetadataGenerator.getAppliesToFor(registeredService));
            return db.queryView(view, CouchDbSamlIdPMetadataDocument.class).stream().findFirst().orElse(null);
        }
        return getForAll();
    }
}
