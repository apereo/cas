package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.couchdb.saml.CouchDbSamlIdPMetadataDocument;
import org.apereo.cas.couchdb.saml.SamlIdPMetadataCouchDbRepository;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.io.ResourceLoader;

import java.io.StringWriter;

/**
 * This is {@link CouchDbSamlIdPMetadataGenerator}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {

    private final CipherExecutor<String, String> metadataCipherExecutor;

    private final SamlIdPMetadataCouchDbRepository couchDb;

    public CouchDbSamlIdPMetadataGenerator(final SamlIdPMetadataLocator samlIdPMetadataLocator,
                                           final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter, final String entityId,
                                           final ResourceLoader resourceLoader, final String casServerPrefix,
                                           final String scope, final CipherExecutor metadataCipherExecutor, final SamlIdPMetadataCouchDbRepository couchDb) {
        super(samlIdPMetadataLocator, samlIdPCertificateAndKeyWriter, entityId, resourceLoader, casServerPrefix, scope);
        this.metadataCipherExecutor = metadataCipherExecutor;
        this.couchDb = couchDb;
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedEncryptionCert() {
        try (val certWriter = new StringWriter(); val keyWriter = new StringWriter()) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
            val encryptionKey = metadataCipherExecutor.encode(keyWriter.toString());
            val doc = getSamlIdPMetadataDocument();
            doc.setEncryptionCertificate(certWriter.toString());
            doc.setEncryptionKey(encryptionKey);
            saveSamlIdPMetadataDocument(doc);
        }
    }

    private CouchDbSamlIdPMetadataDocument getSamlIdPMetadataDocument() {
        val metadata = couchDb.getOne();
        if (metadata == null) {
            return new CouchDbSamlIdPMetadataDocument();
        }
        return metadata;
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedSigningCert() {
        try (val certWriter = new StringWriter(); val keyWriter = new StringWriter()) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
            val signingKey = metadataCipherExecutor.encode(keyWriter.toString());
            val doc = getSamlIdPMetadataDocument();
            doc.setSigningCertificate(certWriter.toString());
            doc.setSigningKey(signingKey);
            saveSamlIdPMetadataDocument(doc);
        }
    }

    @Override
    protected void writeMetadata(final String metadata) {
        val doc = getSamlIdPMetadataDocument();
        doc.setMetadata(metadata);
        saveSamlIdPMetadataDocument(doc);
    }

    private void saveSamlIdPMetadataDocument(final SamlIdPMetadataDocument doc) {
        val couchDoc = couchDb.getOne();
        if (couchDoc == null) {
            couchDb.add(new CouchDbSamlIdPMetadataDocument(doc));
        } else {
            couchDb.update(couchDoc.merge(doc));
        }
    }

}
