package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link MongoDbSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class MongoDbSamlIdPMetadataLocator implements SamlIdPMetadataLocator {
    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;
    private final CipherExecutor<String, String> metadataCipherExecutor;

    private SamlIdPMetadataDocument metadataDocument;

    @Override
    public Resource getSigningCertificate() {
        fetchMetadataDocument();
        val cert = metadataDocument.getSigningCertificate();
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getSigningKey() {
        fetchMetadataDocument();
        val data = metadataDocument.getSigningKey();
        val cert = metadataCipherExecutor.decode(data);
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getMetadata() {
        fetchMetadataDocument();
        val data = metadataDocument.getMetadata();
        return new InputStreamResource(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getEncryptionCertificate() {
        fetchMetadataDocument();
        val cert = metadataDocument.getEncryptionCertificate();
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Resource getEncryptionKey() {
        fetchMetadataDocument();
        val data = metadataDocument.getEncryptionKey();
        val cert = metadataCipherExecutor.decode(data);
        return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void initialize() {
        fetchMetadataDocument();
    }

    private void fetchMetadataDocument() {
        metadataDocument = this.mongoTemplate.findOne(new Query(), SamlIdPMetadataDocument.class, this.collectionName);
    }

    @Override
    public boolean exists() {
        fetchMetadataDocument();
        return metadataDocument != null;
    }
}
