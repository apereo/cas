package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
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
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

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
                : resolveServiceMetadataDirectory(registeredService);
            LOGGER.debug("Metadata directory location for [{}] is [{}]", samlRegisteredService.getName(), serviceDirectory);
            if (serviceDirectory != null && serviceDirectory.exists()) {
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

    /**
     * Resolves the directory holding a service's own identity provider metadata artifacts.
     * <p>
     * That directory is named after the service, and the name is carried into the path verbatim.
     * A service name is operator-supplied and has never been validated as a path component, so one
     * containing {@code ..} walks out of the metadata location and has CAS load signing or
     * encryption key material from wherever it lands. The name itself is deliberately left alone --
     * nothing in CAS generates these directories, deployments provision them by hand, and renaming
     * or hashing the segment would orphan every one that already exists -- so what is enforced is
     * containment: the resolved directory has to sit inside the metadata location.
     * <p>
     * A name that does not is ignored rather than rejected outright, which lands the caller on the
     * global artifacts, the same as any service that has no directory of its own.
     *
     * @param registeredService the registered service
     * @return the directory, or null when it does not resolve inside the metadata location
     * @throws IOException the exception
     */
    protected @Nullable File resolveServiceMetadataDirectory(final Optional<SamlRegisteredService> registeredService) throws IOException {
        val owner = getAppliesToFor(registeredService);
        val metadataRoot = this.metadataLocation.getCanonicalFile().toPath();
        val serviceDirectory = new File(this.metadataLocation, owner).getCanonicalFile().toPath();
        if (!serviceDirectory.startsWith(metadataRoot)) {
            LOGGER.warn("Metadata directory [{}] resolved for [{}] falls outside the metadata location [{}] "
                + "and will be ignored.", serviceDirectory, owner, metadataRoot);
            return null;
        }
        return serviceDirectory.toFile();
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
