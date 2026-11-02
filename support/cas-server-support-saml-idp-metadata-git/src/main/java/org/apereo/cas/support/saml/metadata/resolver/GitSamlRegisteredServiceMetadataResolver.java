package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

    private final CasReentrantLock pullLock = new CasReentrantLock();

    private final Map<String, IndexedFile> entityIdIndex = new ConcurrentHashMap<>();

    private volatile Instant lastPullInstant = Instant.EPOCH;

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
        pullIfNecessary();
        val metadataFiles = listMetadataFiles();
        entityIdIndex.keySet().retainAll(metadataFiles.stream().map(File::getAbsolutePath).collect(Collectors.toSet()));
        val entityId = getEntityIdFromCriteriaSet(criteriaSet);
        return metadataFiles
            .stream()
            .filter(file -> entityId.map(id -> id.equals(getEntityIdOf(file))).orElse(Boolean.TRUE))
            .map(GitSamlRegisteredServiceMetadataResolver::parseFileIntoSamlMetadataDocument)
            .filter(Objects::nonNull)
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
            pullIfNecessary();
            return listMetadataFiles()
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

    /**
     * Pulls changes from the remote repository at most once per the schedule's repeat interval, and shares that pull
     * with every load that asks for it in the meantime. A pull is a network round trip, and loads happen on cache
     * misses, which entity-scoped lookups make frequent. The first load always pulls, so metadata committed to the
     * remote before it is picked up without waiting for the scheduled pull.
     */
    protected void pullIfNecessary() {
        val schedule = samlIdPProperties.getMetadata().getGit().getSchedule();
        val interval = Beans.newDuration(StringUtils.defaultIfBlank(schedule.getRepeatInterval(), "PT2M"));
        if (isPullDue(interval)) {
            pullLock.execute(() -> {
                if (isPullDue(interval)) {
                    try {
                        if (gitRepository.pull()) {
                            LOGGER.debug("Successfully pulled metadata changes from the remote repository");
                        } else {
                            LOGGER.warn("Unable to pull changes from the remote repository. Metadata files may be stale.");
                        }
                    } catch (final Exception e) {
                        LoggingUtils.error(LOGGER, e);
                    } finally {
                        lastPullInstant = Instant.now(Clock.systemUTC());
                    }
                }
            });
        }
    }

    /**
     * Gets the entity id of the metadata document kept in a file, parsing the document only when the file is new or
     * has changed since it was last seen.
     *
     * @param metadataFile the metadata file
     * @return the entity id, or null when it cannot be determined
     */
    protected @Nullable String getEntityIdOf(final File metadataFile) {
        val key = metadataFile.getAbsolutePath();
        val size = metadataFile.length();
        val lastModified = metadataFile.lastModified();
        val indexed = entityIdIndex.get(key);
        if (indexed != null && indexed.size() == size && indexed.lastModified() == lastModified) {
            return indexed.entityId();
        }
        val document = parseFileIntoSamlMetadataDocument(metadataFile);
        val entityId = document != null ? prepareMetadataDocument(document, false).getEntityId() : null;
        entityIdIndex.put(key, new IndexedFile(size, lastModified, entityId));
        return entityId;
    }

    protected Collection<File> listMetadataFiles() {
        return FileUtils.listFiles(getMetadataDirectory(), new String[]{"xml"}, false);
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
        val files = repoDirectory.listFiles((dir, fileName) ->
            getDocumentName(FilenameUtils.removeExtension(fileName)).equalsIgnoreCase(name));
        if (files != null) {
            for (val file : files) {
                removeFile(file);
            }
        }
    }

    /**
     * Get the numeric prefix of a metadata file name, as in {@code 1234-SP}, when there is one.
     *
     * @param baseName the file name without its extension
     * @return the numeric prefix, or empty when the file name does not start with one
     */
    private static Optional<String> getDocumentIdPrefix(final String baseName) {
        val separatorIndex = baseName.indexOf(SEPARATOR);
        return separatorIndex > 0 && NumberUtils.isDigits(baseName.substring(0, separatorIndex))
            ? Optional.of(baseName.substring(0, separatorIndex))
            : Optional.empty();
    }

    /**
     * Get the identifier of the metadata document kept in a file. Files stored by CAS are named
     * {@code <id>-<name>}. A file committed to the repository by hand, such as {@code SP.xml},
     * gets an identifier derived from its name, so the same file keeps the same identifier on every load
     * and can be found and removed by it.
     *
     * @param baseName the file name without its extension
     * @return the document identifier
     */
    private static long getDocumentId(final String baseName) {
        return getDocumentIdPrefix(baseName)
            .map(Long::parseLong)
            .orElseGet(() -> Integer.toUnsignedLong(baseName.hashCode()));
    }

    /**
     * Get the name of the metadata document kept in a file: the file name without its numeric prefix, if any.
     * A name that itself carries the separator, such as {@code default-sp}, is kept whole.
     *
     * @param baseName the file name without its extension
     * @return the document name
     */
    private static String getDocumentName(final String baseName) {
        return getDocumentIdPrefix(baseName)
            .map(prefix -> baseName.substring(prefix.length() + 1))
            .orElse(baseName);
    }

    private static @Nullable SamlMetadataDocument parseFileIntoSamlMetadataDocument(final File metadataFile) {
        try {
            val baseName = FilenameUtils.removeExtension(metadataFile.getName());
            val id = getDocumentId(baseName);
            val name = getDocumentName(baseName);

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

    private boolean isPullDue(final Duration interval) {
        return !Instant.now(Clock.systemUTC()).isBefore(lastPullInstant.plus(interval));
    }

    private record IndexedFile(long size, long lastModified, @Nullable String entityId) {
    }
}
