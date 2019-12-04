package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.couchdb.saml.SamlIdPMetadataCouchDbRepository;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;

import java.util.Optional;

/**
 * This is {@link CouchDbSamlIdPMetadataLocator}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {

    private final SamlIdPMetadataCouchDbRepository couchDb;

    public CouchDbSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                         final SamlIdPMetadataCouchDbRepository couchDb) {
        super(metadataCipherExecutor);
        this.couchDb = couchDb;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isPresent()) {
            val doc = couchDb.getForService(registeredService);
            if (doc != null && doc.isValid()) {
                return doc;
            }
        }
        return couchDb.getForAll();
    }
}
