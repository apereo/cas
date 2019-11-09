package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

/**
 * This is {@link MongoDbSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class MongoDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;

    public MongoDbSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
                                           final MongoTemplate mongoTemplate, final String collectionName) {
        super(samlIdPMetadataGeneratorConfigurationContext);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        val results = generateCertificateAndKey();
        var update = Update.update("encryptionCertificate", results.getKey()).addToSet("encryptionKey", results.getValue());
        this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        return results;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        val results = generateCertificateAndKey();
        val update = Update.update("signingCertificate", results.getKey()).addToSet("signingKey", results.getValue());
        this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        return results;
    }

    @Override
    protected String writeMetadata(final String metadata, final Optional<SamlRegisteredService> registeredService) {
        val update = Update.update("metadata", metadata);
        this.mongoTemplate.upsert(new Query(), update, SamlIdPMetadataDocument.class, this.collectionName);
        return metadata;
    }
}
