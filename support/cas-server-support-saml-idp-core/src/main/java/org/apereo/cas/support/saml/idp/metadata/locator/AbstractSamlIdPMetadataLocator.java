package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link AbstractSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public abstract class AbstractSamlIdPMetadataLocator implements SamlIdPMetadataLocator {
    /**
     * Cipher executor to encrypt/sign metadata.
     */
    protected final CipherExecutor<String, String> metadataCipherExecutor;
    /**
     * The idp metadata document fetched from storage.
     */
    protected SamlIdPMetadataDocument metadataDocument = new SamlIdPMetadataDocument();

    @Override
    public Resource getSigningCertificate() {
        if (exists()) {
            val cert = metadataDocument.getSigningCertificate();
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getSigningKey() {
        if (exists()) {
            val data = metadataDocument.getSigningKey();
            val cert = metadataCipherExecutor.decode(data);
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getMetadata() {
        if (exists()) {
            val data = metadataDocument.getMetadata();
            return new InputStreamResource(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getEncryptionCertificate() {
        if (exists()) {
            val cert = metadataDocument.getEncryptionCertificate();
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public Resource getEncryptionKey() {
        if (exists()) {
            val data = metadataDocument.getEncryptionKey();
            val cert = metadataCipherExecutor.decode(data);
            return new InputStreamResource(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    @Override
    public void initialize() {
        fetch();
    }


    @Override
    public boolean exists() {
        fetch();
        return metadataDocument != null && metadataDocument.isValid();
    }
}
