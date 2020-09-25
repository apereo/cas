package org.apereo.cas.services.locator;

import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;

/**
 * This is {@link TypeAwareGitRepositoryRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class DefaultGitRepositoryRegisteredServiceLocator implements GitRepositoryRegisteredServiceLocator {
    private final RegisteredServiceResourceNamingStrategy resourceNamingStrategy;

    private final File repositoryDirectory;

    private final GitServiceRegistryProperties properties;

    @Override
    @SneakyThrows
    public File determine(final RegisteredService service, final String extension) {
        val fileName = resourceNamingStrategy.build(service, extension);
        val parentDirectory = GitRepositoryRegisteredServiceLocator.normalizeParentDirectory(
            repositoryDirectory, properties.getRootDirectory());
        return new File(parentDirectory, fileName);
    }
}
