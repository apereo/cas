package org.apereo.cas.services.locator;

import module java.base;
import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.eclipse.jgit.util.FileUtils;

/**
 * This is {@link TypeAwareGitRepositoryRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class TypeAwareGitRepositoryRegisteredServiceLocator implements GitRepositoryRegisteredServiceLocator {
    private final RegisteredServiceResourceNamingStrategy resourceNamingStrategy;

    private final File repositoryDirectory;

    private final GitServiceRegistryProperties properties;

    @Override
    public File determine(final RegisteredService service, final String extension) {
        return FunctionUtils.doUnchecked(() -> {
            val fileName = resourceNamingStrategy.build(service, extension);
            var parentDirectory = GitRepositoryRegisteredServiceLocator.normalizeParentDirectory(
                repositoryDirectory, properties.getRootDirectory());
            parentDirectory = new File(parentDirectory, service.getFriendlyName());
            FileUtils.mkdir(parentDirectory, true);
            return new File(parentDirectory, fileName);
        });
    }
}
