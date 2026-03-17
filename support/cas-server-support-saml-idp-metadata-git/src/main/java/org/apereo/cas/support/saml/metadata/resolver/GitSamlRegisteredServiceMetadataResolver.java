package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

/**
 * This is {@link GitSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GitSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    private static final char SEPARATOR = '-';

    private final GitRepository gitRepository;

    public GitSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                    final OpenSamlConfigBean configBean,
                                                    final GitRepository gitRepository) {
        super(samlIdPProperties, configBean);
        this.gitRepository = gitRepository;
    }


    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service,
                                                          final CriteriaSet criteriaSet) {
        return load()
            .stream()
            .map(doc -> buildMetadataResolverFrom(service, doc))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        if (service == null) {
            return false;
        }
        val metadataLocation = service.getMetadataLocation();
        return metadataLocation != null
            && (metadataLocation.trim().startsWith(getSourceId())
            || (metadataLocation.trim().startsWith("http") && metadataLocation.trim().endsWith(".git")));
    }

    @Override
    public String getSourceId() {
        return "git://";
    }

    @Override
    public List<SamlMetadataDocument> load() {
        try {
            if (gitRepository.pull()) {
                LOGGER.debug("Successfully pulled metadata changes from the remote repository");
            } else {
                LOGGER.warn("Unable to pull changes from the remote repository. Metadata files may be stale.");
            }
            val repoDirectory = getMetadataDirectory();
            val metadataFiles = FileUtils.listFiles(repoDirectory, new String[]{"xml"}, false);
            return metadataFiles
                .stream()
                .map(GitSamlRegisteredServiceMetadataResolver::parseFileIntoSamlMetadataDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return List.of();
    }

    @Override
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        try {
            document.assignIdIfNecessary();

            val repoDirectory = getMetadataDirectory();
            removeDocumentFiles(repoDirectory, document.getName());

            val filePrefix = document.getId() + String.valueOf(SEPARATOR) + document.getName();
            val metadataFile = new File(repoDirectory, filePrefix + ".xml");
            val signatureFile = new File(repoDirectory, filePrefix + ".pem");

            FileUtils.writeStringToFile(metadataFile, document.getValue(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(signatureFile, document.getSignature(), StandardCharsets.UTF_8);
            gitRepository.commitAll("Committed " + document.getName());
            gitRepository.push();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return document;
    }

    @Override
    public void removeById(final long id) {
        findById(id).ifPresent(document -> removeByName(document.getName()));
    }

    @Override
    public void removeByName(final String name) {
        try {
            val repoDirectory = getMetadataDirectory();
            removeDocumentFiles(repoDirectory, name);
            gitRepository.commitAll("Removed " + name);
            gitRepository.push();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        return load()
            .stream()
            .filter(document -> name.equalsIgnoreCase(document.getName()))
            .findFirst();
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        return load()
            .stream()
            .filter(document -> document.getId() == id)
            .findFirst();
    }

    @Override
    public void removeAll() {
        try {
            val repoDirectory = getMetadataDirectory();
            FileUtils.cleanDirectory(repoDirectory);
            gitRepository.commitAll("Removed all metadata documents");
            gitRepository.push();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }


    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }

    protected File getMetadataDirectory() {
        val directory = new File(gitRepository.getRepositoryDirectory(), "sp-metadata");
        if (directory.mkdirs()) {
            LOGGER.trace("Created metadata directory [{}]", directory);
        }
        return directory;
    }

    private static void removeFile(final File metadataFile) throws IOException {
        val result = !metadataFile.exists() || metadataFile.delete();
        LOGGER.trace("Deleted service definition file [{}]: [{}]",
            metadataFile.getCanonicalPath(), BooleanUtils.toStringYesNo(result));
    }

    private static void removeDocumentFiles(final File repoDirectory, final String name) throws IOException {
        val files = repoDirectory.listFiles((dir, fileName) -> {
            val baseName = FilenameUtils.removeExtension(fileName);
            val parsed = baseName.indexOf(SEPARATOR);
            return parsed > 0 && baseName.substring(parsed + 1).equalsIgnoreCase(name);
        });
        if (files != null) {
            for (val file : files) {
                removeFile(file);
            }
        }
    }

    private static @Nullable SamlMetadataDocument parseFileIntoSamlMetadataDocument(final File metadataFile) {
        try {
            val baseName = FilenameUtils.removeExtension(metadataFile.getName());
            val separatorIndex = baseName.indexOf(SEPARATOR);
            val id = separatorIndex > 0 ? Long.parseLong(baseName.substring(0, separatorIndex)) : System.nanoTime();
            val name = separatorIndex > 0 ? baseName.substring(separatorIndex + 1) : baseName;

            val value = FileUtils.readFileToString(metadataFile, StandardCharsets.UTF_8);
            val signatureFile = new File(metadataFile.getParentFile(), baseName + ".pem");
            val signature = signatureFile.exists()
                ? FileUtils.readFileToString(signatureFile, StandardCharsets.UTF_8)
                : StringUtils.EMPTY;

            return SamlMetadataDocument.builder()
                .id(id)
                .name(name)
                .value(value)
                .signature(signature)
                .build();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }
}
