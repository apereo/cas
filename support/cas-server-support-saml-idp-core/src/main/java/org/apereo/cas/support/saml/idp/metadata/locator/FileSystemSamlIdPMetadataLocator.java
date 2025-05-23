package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
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
@Monitorable
public class FileSystemSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private final File metadataLocation;

    public FileSystemSamlIdPMetadataLocator(final CipherExecutor cipherExecutor,
                                            final Resource resource, final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                            final ConfigurableApplicationContext applicationContext) throws Exception {
        this(cipherExecutor, resource.getFile(), metadataCache, applicationContext);
    }

    public FileSystemSamlIdPMetadataLocator(final CipherExecutor cipherExecutor, final File resource,
                                            final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                            final ConfigurableApplicationContext applicationContext) {
        super(cipherExecutor, metadataCache, applicationContext);
        this.metadataLocation = resource;
    }

    @Override
    public Resource resolveSigningCertificate(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return getMetadataArtifact(registeredService, "idp-signing.crt");
    }

    @Override
    public Resource resolveSigningKey(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return getMetadataArtifact(registeredService, "idp-signing.key");
    }

    @Override
    public Resource resolveMetadata(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return getMetadataArtifact(registeredService, "idp-metadata.xml");
    }

    @Override
    public Resource resolveEncryptionCertificate(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return getMetadataArtifact(registeredService, "idp-encryption.crt");
    }

    @Override
    public Resource resolveEncryptionKey(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return getMetadataArtifact(registeredService, "idp-encryption.key");
    }

    @Override
    public boolean exists(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        return resolveMetadata(registeredService).exists();
    }

    @Override
    public void initialize() {
        initializeMetadataDirectory();
        LOGGER.info("Metadata directory location is at [{}]", this.metadataLocation);
    }

    @Override
    protected SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return FunctionUtils.doUnchecked(() -> {
            val doc = new SamlIdPMetadataDocument();
            try (val in = resolveMetadata(registeredService).getInputStream()) {
                doc.setMetadata(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            try (val in = resolveEncryptionCertificate(registeredService).getInputStream()) {
                doc.setEncryptionCertificate(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            try (val in = resolveEncryptionKey(registeredService).getInputStream()) {
                doc.setEncryptionKey(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            try (val in = resolveSigningCertificate(registeredService).getInputStream()) {
                doc.setSigningCertificate(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            try (val in = resolveSigningKey(registeredService).getInputStream()) {
                doc.setSigningKey(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
            doc.setAppliesTo(getAppliesToFor(registeredService));
            return doc;
        });
    }

    protected Resource getMetadataArtifact(final Optional<SamlRegisteredService> registeredService, final String artifactName) throws Throwable {
        if (registeredService.isPresent()) {
            val samlRegisteredService = registeredService.get();
            val serviceDirectory = StringUtils.isNotBlank(samlRegisteredService.getIdpMetadataLocation())
                ? ResourceUtils.getRawResourceFrom(SpringExpressionLanguageValueResolver.getInstance().resolve(samlRegisteredService.getIdpMetadataLocation())).getFile()
                : new File(this.metadataLocation, getAppliesToFor(registeredService));
            LOGGER.debug("Metadata directory location for [{}] is [{}]", samlRegisteredService.getName(), serviceDirectory);
            if (serviceDirectory.exists()) {
                val artifact = new File(serviceDirectory, artifactName);
                LOGGER.trace("Artifact location for [{}] and [{}] is [{}]", artifactName, samlRegisteredService.getName(), artifact);
                if (artifact.exists()) {
                    LOGGER.debug("Using metadata artifact [{}] at [{}]", artifactName, artifact);
                    return ResourceUtils.toFileSystemResource(artifact);
                }
            }
        }
        initializeMetadataDirectory();
        val resource = ResourceUtils.toFileSystemResource(new File(this.metadataLocation, artifactName));
        if (resource.exists() && resource.isReadable()) {
            val content = FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(content)) {
                return resolveContentToResource(content);
            }
            LOGGER.warn("Metadata artifact at [{}] is empty and invalid and will be deleted", resource);
            FileUtils.deleteQuietly(resource.getFile());
        }
        return ResourceUtils.toFileSystemResource(resource.getFile());
    }

    protected void initializeMetadataDirectory() {
        if (!this.metadataLocation.exists()) {
            LOGGER.debug("Metadata directory [{}] does not exist. Creating...", this.metadataLocation);
            if (!this.metadataLocation.mkdir()) {
                throw new IllegalArgumentException("Metadata directory location " + this.metadataLocation + " cannot be located/created");
            }
        }
    }
}
