package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.couchdb.saml.CouchDbSamlIdPMetadataDocument;
import org.apereo.cas.couchdb.saml.SamlIdPMetadataCouchDbRepository;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

/**
 * This is {@link CouchDbSamlIdPMetadataGenerator}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {

    private final SamlIdPMetadataCouchDbRepository couchDb;

    public CouchDbSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                           final SamlIdPMetadataCouchDbRepository couchDb) {
        super(context);
        this.couchDb = couchDb;
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        var couchDoc = registeredService.isPresent()
            ? couchDb.getForService(registeredService)
            : couchDb.getForAll();
        if (couchDoc == null) {
            couchDoc = new CouchDbSamlIdPMetadataDocument(doc);
            couchDoc.setAppliesTo(couchDb.getAppliesToFor(registeredService));
            couchDb.add(couchDoc);
        } else {
            couchDb.update(couchDoc.merge(doc));
        }
        return couchDoc;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }
}
