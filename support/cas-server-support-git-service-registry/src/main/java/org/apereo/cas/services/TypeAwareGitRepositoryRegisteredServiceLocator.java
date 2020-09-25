package org.apereo.cas.services;

import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;

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

    @Override
    @SneakyThrows
    public File determine(final RegisteredService service, final String extension) {
        val fileName = resourceNamingStrategy.build(service, extension);
        val parentDirectory = new File(repositoryDirectory, service.getFriendlyName());
        FileUtils.mkdir(parentDirectory, true);
        return new File(parentDirectory, fileName);
    }
}
