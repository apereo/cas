package org.apereo.cas.services.locator;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link GitRepositoryRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface GitRepositoryRegisteredServiceLocator {
    /**
     * The File extensions.
     */
    List<String> FILE_EXTENSIONS = CollectionUtils.wrapList("json", "yaml", "yml");

    /**
     * The constant PATTERN_ACCEPTED_REPOSITORY_FILES.
     */
    Pattern PATTERN_ACCEPTED_REPOSITORY_FILES = RegexUtils.createPattern(".+\\.("
        + String.join("|", FILE_EXTENSIONS) + ')', Pattern.CASE_INSENSITIVE);

    /**
     * Determine file.
     *
     * @param service   the service
     * @param extension the extension
     * @return the file
     */
    File determine(RegisteredService service, String extension);

    /**
     * Locate file that is linked to the service.
     *
     * @param service the service
     * @return the optional
     */
    default Optional<File> locate(final RegisteredService service) {
        return FILE_EXTENSIONS.stream()
            .map(ext -> determine(service, ext))
            .filter(File::exists)
            .findFirst();
    }

    /**
     * Normalize parent directory file.
     *
     * @param repositoryDirectory the repository directory
     * @param rootDirectory       the root directory
     * @return the file
     * @throws IOException the io exception
     */
    static File normalizeParentDirectory(final File repositoryDirectory, final String rootDirectory) throws IOException {
        var parentDirectory = repositoryDirectory;
        if (StringUtils.isNotBlank(rootDirectory)) {
            parentDirectory = new File(repositoryDirectory, rootDirectory);
            FileUtils.mkdir(parentDirectory, true);
        }
        return parentDirectory;
    }
}
