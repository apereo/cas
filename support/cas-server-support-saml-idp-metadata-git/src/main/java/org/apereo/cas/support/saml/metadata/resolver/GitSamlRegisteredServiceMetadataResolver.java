package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.PathRegexPatternTreeFilter;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link GitSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GitSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Pattern PATTERN_METADATA_FILES = RegexUtils.createPattern(".+\\.xml", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_SIGNATURE_FILES = RegexUtils.createPattern(".+\\.pem", Pattern.CASE_INSENSITIVE);

    private final GitRepository gitRepository;

    public GitSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                    final OpenSamlConfigBean configBean,
                                                    final GitRepository gitRepository) {
        super(samlIdPProperties, configBean);
        this.gitRepository = gitRepository;
    }

    private static void removeFile(final File metadataFile) throws IOException {
        val result = !metadataFile.exists() || metadataFile.delete();
        LOGGER.trace("Deleted service definition file [{}]: [{}]",
            metadataFile.getCanonicalPath(), BooleanUtils.toStringYesNo(result));
    }

    private static SamlMetadataDocument parseGitObjectContentIntoSamlMetadataDocument(final GitRepository.GitObject gitObject,
                                                                                      final Collection<GitRepository.GitObject> signatureFiles) {
        val name = FilenameUtils.removeExtension(gitObject.getPath());
        val signature = signatureFiles.stream()
            .filter(sigFile -> name.equalsIgnoreCase(FilenameUtils.removeExtension(sigFile.getPath())))
            .findFirst()
            .map(GitRepository.GitObject::getContent)
            .orElse(StringUtils.EMPTY);

        return SamlMetadataDocument.builder()
            .id(System.nanoTime())
            .name(name)
            .value(gitObject.getContent())
            .signature(signature)
            .build();
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        if (gitRepository.pull()) {
            LOGGER.debug("Successfully pulled metadata changes from the remote repository");
        } else {
            LOGGER.warn("Unable to pull changes from the remote repository. Metadata files may be stale.");
        }
        val metadataFiles = this.gitRepository.getObjectsInRepository(new PathRegexPatternTreeFilter(PATTERN_METADATA_FILES));
        val signatureFiles = this.gitRepository.getObjectsInRepository(new PathRegexPatternTreeFilter(PATTERN_SIGNATURE_FILES));

        return metadataFiles
            .stream()
            .filter(Objects::nonNull)
            .map(object -> parseGitObjectContentIntoSamlMetadataDocument(object, signatureFiles))
            .map(doc -> buildMetadataResolverFrom(service, doc))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        if (service == null) {
            return false;
        }
        val metadataLocation = service.getMetadataLocation();
        return metadataLocation != null
            && (metadataLocation.trim().startsWith("git://")
            || (metadataLocation.trim().startsWith("http") && metadataLocation.trim().endsWith(".git")));
    }

    @Override
    public void saveOrUpdate(final SamlMetadataDocument document) {
        try {
            val repoDirectory = this.gitRepository.getRepositoryDirectory();

            val metadataFile = new File(repoDirectory, document.getName() + ".xml");
            removeFile(metadataFile);

            val signatureFile = new File(repoDirectory, document.getName() + ".pem");
            removeFile(signatureFile);

            FileUtils.writeStringToFile(metadataFile, document.getValue(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(signatureFile, document.getSignature(), StandardCharsets.UTF_8);
            this.gitRepository.commitAll("Committed " + document.getName());
            this.gitRepository.push();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }
}
