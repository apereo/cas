package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link FileSystemSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
public class FileSystemSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final File metadataLocation;

    public FileSystemSamlIdPMetadataLocator(final Resource resource, final Cache<String, SamlIdPMetadataDocument> metadataCache) throws Exception {
        this(resource.getFile(), metadataCache);
    }

    public FileSystemSamlIdPMetadataLocator(final File resource, final Cache<String, SamlIdPMetadataDocument> metadataCache) {
        super(CipherExecutor.noOpOfStringToString(), metadataCache);
        this.metadataLocation = resource;
    }

    @Override
    public Resource resolveSigningCertificate(final Optional<SamlRegisteredService> registeredService) {
        return getMetadataArtifact(registeredService, "idp-signing.crt");
    }

    @Override
    public Resource resolveSigningKey(final Optional<SamlRegisteredService> registeredService) {
        return getMetadataArtifact(registeredService, "idp-signing.key");
    }

    @Override
    public Resource resolveMetadata(final Optional<SamlRegisteredService> registeredService) {
        return getMetadataArtifact(registeredService, "idp-metadata.xml");
    }

    @Override
    public Resource getEncryptionCertificate(final Optional<SamlRegisteredService> registeredService) {
        return getMetadataArtifact(registeredService, "idp-encryption.crt");
    }

    @Override
    public Resource resolveEncryptionKey(final Optional<SamlRegisteredService> registeredService) {
        return getMetadataArtifact(registeredService, "idp-encryption.key");
    }

    @Override
    public boolean exists(final Optional<SamlRegisteredService> registeredService) {
        return resolveMetadata(registeredService).exists();
    }

    @SneakyThrows
    @Override
    protected SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val doc = new SamlIdPMetadataDocument();
        doc.setMetadata(IOUtils.toString(resolveMetadata(registeredService).getInputStream(), StandardCharsets.UTF_8));
        doc.setEncryptionCertificate(IOUtils.toString(getEncryptionCertificate(registeredService).getInputStream(), StandardCharsets.UTF_8));
        doc.setEncryptionKey(IOUtils.toString(resolveEncryptionKey(registeredService).getInputStream(), StandardCharsets.UTF_8));
        doc.setSigningCertificate(IOUtils.toString(resolveSigningCertificate(registeredService).getInputStream(), StandardCharsets.UTF_8));
        doc.setSigningKey(IOUtils.toString(resolveSigningKey(registeredService).getInputStream(), StandardCharsets.UTF_8));
        doc.setAppliesTo(SamlIdPMetadataGenerator.getAppliesToFor(registeredService));
        return doc;
    }

    @Override
    public void initialize() {
        initializeMetadataDirectory();
        LOGGER.info("Metadata directory location is at [{}]", this.metadataLocation);
    }

    private void initializeMetadataDirectory() {
        if (!this.metadataLocation.exists()) {
            LOGGER.debug("Metadata directory [{}] does not exist. Creating...", this.metadataLocation);
            if (!this.metadataLocation.mkdir()) {
                throw new IllegalArgumentException("Metadata directory location " + this.metadataLocation + " cannot be located/created");
            }
        }
    }

    /**
     * Gets metadata artifact.
     *
     * @param result       the result
     * @param artifactName the artifact name
     * @return the metadata artifact
     */
    protected Resource getMetadataArtifact(final Optional<SamlRegisteredService> result, final String artifactName) {
        if (result.isPresent()) {
            val serviceDirectory = new File(this.metadataLocation, SamlIdPMetadataGenerator.getAppliesToFor(result));
            LOGGER.trace("Metadata directory location for [{}] is [{}]", result.get().getName(), serviceDirectory);
            if (serviceDirectory.exists()) {
                val artifact = new File(serviceDirectory, artifactName);
                LOGGER.trace("Artifact location for [{}] and [{}] is [{}]", artifactName, result.get().getName(), artifact);
                if (artifact.exists()) {
                    LOGGER.debug("Using metadata artifact [{}] at [{}]", artifactName, artifact);
                    return new FileSystemResource(artifact);
                }
            }
        }
        initializeMetadataDirectory();
        return new FileSystemResource(new File(this.metadataLocation, artifactName));
    }
}
