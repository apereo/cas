package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.StringWriter;

/**
 * This is {@link MongoDbSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class MongoDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;
    private final CipherExecutor<String, String> metadataCipherExecutor;

    public MongoDbSamlIdPMetadataGenerator(final SamlIdPMetadataLocator samlIdPMetadataLocator,
                                           final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter,
                                           final String entityId,
                                           final ResourceLoader resourceLoader,
                                           final String casServerPrefix,
                                           final String scope,
                                           final MongoTemplate mongoTemplate,
                                           final String collectionName,
                                           final CipherExecutor metadataCipherExecutor) {
        super(samlIdPMetadataLocator, samlIdPCertificateAndKeyWriter, entityId, resourceLoader, casServerPrefix, scope);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.metadataCipherExecutor = metadataCipherExecutor;
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedEncryptionCert() {
        try (val certWriter = new StringWriter(); val keyWriter = new StringWriter()) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
            val encryptionKey = metadataCipherExecutor.encode(keyWriter.toString());
            var update = Update.update("encryptionCertificate", certWriter.toString()).addToSet("encryptionKey", encryptionKey);
            this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        }
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedSigningCert() {
        try (val certWriter = new StringWriter(); val keyWriter = new StringWriter()) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
            val signingKey = metadataCipherExecutor.encode(keyWriter.toString());
            val update = Update.update("signingCertificate", certWriter.toString()).addToSet("signingKey", signingKey);
            this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        }
    }

    @Override
    protected void writeMetadata(final String metadata) {
        val update = Update.update("metadata", metadata);
        this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
    }
}
