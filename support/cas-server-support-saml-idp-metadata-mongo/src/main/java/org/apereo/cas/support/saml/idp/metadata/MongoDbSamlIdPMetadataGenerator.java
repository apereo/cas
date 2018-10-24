package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * This is {@link MongoDbSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class MongoDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;

    public MongoDbSamlIdPMetadataGenerator(final SamlIdPMetadataLocator samlIdPMetadataLocator,
                                           final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter,
                                           final String entityId,
                                           final ResourceLoader resourceLoader,
                                           final String casServerPrefix,
                                           final String scope,
                                           final MongoTemplate mongoTemplate,
                                           final String collectionName,
                                           final CipherExecutor metadataCipherExecutor) {
        super(samlIdPMetadataLocator, samlIdPCertificateAndKeyWriter, metadataCipherExecutor, entityId, resourceLoader, casServerPrefix, scope);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert() {
        val results = generateCertificateAndKey();
        var update = Update.update("encryptionCertificate", results.getKey()).addToSet("encryptionKey", results.getValue());
        this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        return results;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert() {
        val results = generateCertificateAndKey();
        val update = Update.update("signingCertificate", results.getKey()).addToSet("signingKey", results.getValue());
        this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        return results;
    }

    @Override
    protected String writeMetadata(final String metadata) {
        val update = Update.update("metadata", metadata);
        this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        return metadata;
    }
}
