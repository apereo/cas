package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.couchdb.saml.SamlIdPMetadataCouchDbRepository;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link CouchDbSamlIdPMetadataLocator}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class CouchDbSamlIdPMetadataLocator implements SamlIdPMetadataLocator {
    private final CipherExecutor<String, String> metadataCipherExecutor;

    private final SamlIdPMetadataCouchDbRepository couchDb;

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

    private void fetchMetadataDocument() {
        metadataDocument = couchDb.getOne();
    }

    @Override
    public boolean exists() {
        fetchMetadataDocument();
        return metadataDocument != null;
    }
}
