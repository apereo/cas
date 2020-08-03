package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.git.GitRepository;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

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

    public GitSamlIdPMetadataLocator(final GitRepository gitRepository) {
        super(gitRepository.getRepositoryDirectory());
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

        val directory = getMetadataDirectory(registeredService);

        val metadataFile = new File(directory, "idp-metadata.xml");
        val signingKey = new File(directory, "idp-signing.key");
        val signingCert = new File(directory, "idp-signing.crt");
        val encryptionKey = new File(directory, "idp-encryption.key");
        val encryptionCert = new File(directory, "idp-encryption.crt");

        return SamlIdPMetadataDocument.builder()
            .appliesTo(getAppliesToFor(registeredService))
            .encryptionCertificate(readFromFile(encryptionCert))
            .encryptionKey(readFromFile(encryptionKey))
            .signingCertificate(readFromFile(signingCert))
            .signingKey(readFromFile(signingKey))
            .metadata(readFromFile(metadataFile))
            .build();
    }

    private File getMetadataDirectory(final Optional<SamlRegisteredService> registeredService) {
        val path = getAppliesToFor(registeredService);
        val directory = new File(gitRepository.getRepositoryDirectory(), path);
        if (!directory.exists() && !directory.mkdir()) {
            throw new IllegalArgumentException("Metadata directory location " + directory + " cannot be located/created");
        }
        return directory;
    }

    private static String readFromFile(final File file) throws IOException {
        return file.exists() && file.canRead() && file.length() > 0
            ? FileUtils.readFileToString(file, StandardCharsets.UTF_8)
            : StringUtils.EMPTY;
    }

    private static String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
    }

    @Override
    protected Resource getMetadataArtifact(final Optional<SamlRegisteredService> registeredService, final String artifactName) {
        val directory = getMetadataDirectory(registeredService);
        return new FileSystemResource(new File(directory, artifactName));
    }
}
