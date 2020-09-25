package org.apereo.cas.services;

import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;

import lombok.RequiredArgsConstructor;
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

    @Override
    public File determine(final RegisteredService service, final String extension) {
        val fileName = resourceNamingStrategy.build(service, extension);
        return new File(repositoryDirectory, fileName);
    }
}
