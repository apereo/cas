package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.git.GitRepository;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link GitSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GitSamlIdPMetadataLocator extends FileSystemSamlIdPMetadataLocator {
    private final GitRepository gitRepository;

    public GitSamlIdPMetadataLocator(final GitRepository gitRepository, final Cache<String, SamlIdPMetadataDocument> metadataCache) {
        super(gitRepository.getRepositoryDirectory(), metadataCache);
        this.gitRepository = gitRepository;
    }

    @SneakyThrows
    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        if (gitRepository.pull()) {
            LOGGER.debug("Successfully pulled metadata changes from the remote repository");
        } else {
            LOGGER.warn("Unable to pull changes from the remote repository. Metadata files may be stale.");
        }

        val metadataFile = getMetadataArtifactFile(registeredService, "idp-metadata.xml");
        LOGGER.trace("IdP metadata file to use is [{}]", metadataFile);

        val signingKey = getMetadataArtifactFile(registeredService, "idp-signing.key");
        LOGGER.trace("IdP metadata signing key file to use is [{}]", metadataFile);

        val signingCert = getMetadataArtifactFile(registeredService, "idp-signing.crt");
        LOGGER.trace("IdP metadata signing certificate file to use is [{}]", metadataFile);

        val encryptionKey = getMetadataArtifactFile(registeredService, "idp-encryption.key");
        LOGGER.trace("IdP metadata encryption key file to use is [{}]", metadataFile);

        val encryptionCert = getMetadataArtifactFile(registeredService, "idp-encryption.crt");
        LOGGER.trace("IdP metadata encryption certificate file to use is [{}]", metadataFile);

        return SamlIdPMetadataDocument.builder()
            .appliesTo(SamlIdPMetadataGenerator.getAppliesToFor(registeredService))
            .encryptionCertificate(readFromFile(encryptionCert))
            .encryptionKey(readFromFile(encryptionKey))
            .signingCertificate(readFromFile(signingCert))
            .signingKey(readFromFile(signingKey))
            .metadata(readFromFile(metadataFile))
            .build();
    }

    @Override
    protected Resource getMetadataArtifact(final Optional<SamlRegisteredService> registeredService, final String artifactName) {
        val file = getMetadataArtifactFile(registeredService, artifactName);
        return new FileSystemResource(file);
    }

    private File getMetadataDirectory(final Optional<SamlRegisteredService> registeredService) {
        val path = SamlIdPMetadataGenerator.getAppliesToFor(registeredService);
        val directory = new File(gitRepository.getRepositoryDirectory(), path);
        if (!directory.exists() && registeredService.isEmpty() && !directory.mkdir()) {
            throw new IllegalArgumentException("Metadata directory location " + directory + " cannot be located/created");
        }
        return directory;
    }

    private File getMetadataArtifactFile(final Optional<SamlRegisteredService> registeredService,
                                         final String fileName) {
        val defaultMetadataDirectory = getMetadataDirectory(Optional.empty());
        val directory = getMetadataDirectory(registeredService);
        val file = new File(directory, fileName);
        if (file.exists() && file.canRead() && file.length() > 0) {
            return file;
        }
        return new File(defaultMetadataDirectory, fileName);
    }

    private static String readFromFile(final File file) throws IOException {
        return file.exists() && file.canRead() && file.length() > 0
            ? FileUtils.readFileToString(file, StandardCharsets.UTF_8)
            : StringUtils.EMPTY;
    }
}
