package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link FileSystemSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class FileSystemSamlIdPMetadataLocator implements SamlIdPMetadataLocator {
    private final File metadataLocation;

    @SneakyThrows
    public FileSystemSamlIdPMetadataLocator(final Resource metadataResource) {
        this.metadataLocation = metadataResource.getFile();
    }

    @Override
    public Resource getSigningCertificate() {
        return new FileSystemResource(new File(metadataLocation, "/idp-signing.crt"));
    }

    @Override
    public Resource getSigningKey() {
        return new FileSystemResource(new File(metadataLocation, "/idp-signing.key"));
    }

    @Override
    public Resource getMetadata() {
        return new FileSystemResource(new File(metadataLocation, "idp-metadata.xml"));
    }

    @Override
    public Resource getEncryptionCertificate() {
        return new FileSystemResource(new File(metadataLocation, "/idp-encryption.crt"));
    }

    @Override
    public Resource getEncryptionKey() {
        return new FileSystemResource(new File(metadataLocation, "/idp-encryption.key"));
    }

    @Override
    public void initialize() {
        if (!this.metadataLocation.exists()) {
            LOGGER.debug("Metadata directory [{}] does not exist. Creating...", this.metadataLocation);
            if (!this.metadataLocation.mkdir()) {
                throw new IllegalArgumentException("Metadata directory location " + this.metadataLocation + " cannot be located/created");
            }
        }
        LOGGER.info("Metadata directory location is at [{}]", this.metadataLocation);
    }

    @Override
    public boolean exists() {
        return getMetadata().exists();
    }

    @SneakyThrows
    @Override
    public SamlIdPMetadataDocument fetch() {
        val doc = new SamlIdPMetadataDocument();
        doc.setMetadata(IOUtils.toString(getMetadata().getInputStream(), StandardCharsets.UTF_8));
        doc.setEncryptionCertificate(IOUtils.toString(getEncryptionCertificate().getInputStream(), StandardCharsets.UTF_8));
        doc.setEncryptionKey(IOUtils.toString(getEncryptionKey().getInputStream(), StandardCharsets.UTF_8));
        doc.setSigningCertificate(IOUtils.toString(getSigningCertificate().getInputStream(), StandardCharsets.UTF_8));
        doc.setSigningKey(IOUtils.toString(getSigningKey().getInputStream(), StandardCharsets.UTF_8));
        return doc;
    }
}
