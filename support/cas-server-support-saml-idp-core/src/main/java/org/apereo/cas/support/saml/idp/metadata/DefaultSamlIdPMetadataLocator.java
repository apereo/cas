package org.apereo.cas.support.saml.idp.metadata;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * This is {@link DefaultSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSamlIdPMetadataLocator implements SamlIdPMetadataLocator {
    private final File metadataLocation;

    @SneakyThrows
    public DefaultSamlIdPMetadataLocator(final Resource metadataResource) {
        this.metadataLocation = metadataResource.getFile();
    }

    @Override
    public Resource getIdPSigningCertFile() {
        return new FileSystemResource(new File(metadataLocation, "/idp-signing.crt"));
    }

    @Override
    public Resource getIdPSigningKeyFile() {
        return new FileSystemResource(new File(metadataLocation, "/idp-signing.key"));
    }

    @Override
    public File getIdPMetadataFile() {
        return new File(metadataLocation, "idp-metadata.xml");
    }

    @Override
    public Resource getIdPEncryptionCertFile() {
        return new FileSystemResource(new File(metadataLocation, "/idp-encryption.crt"));
    }

    @Override
    public Resource getIdPEncryptionKeyFile() {
        return new FileSystemResource(new File(metadataLocation, "/idp-encryption.key"));
    }

    @Override
    public File getConfigurationLocation() {
        return this.metadataLocation;
    }

    @Override
    public void initialize() {
        if (!getConfigurationLocation().exists()) {
            LOGGER.debug("Metadata directory [{}] does not exist. Creating...", getConfigurationLocation());
            if (!getConfigurationLocation().mkdir()) {
                throw new IllegalArgumentException("Metadata directory location " + getConfigurationLocation() + " cannot be located/created");
            }
        }
    }
}
