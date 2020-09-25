package org.apereo.cas.services;

import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.PathRegexPatternTreeFilter;
import org.apereo.cas.services.locator.GitRepositoryRegisteredServiceLocator;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link GitServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GitServiceRegistry extends AbstractServiceRegistry {
    private final GitRepository gitRepository;

    private final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers;

    private final boolean pushChanges;

    private final List<GitRepositoryRegisteredServiceLocator> registeredServiceLocators;

    private Collection<RegisteredService> registeredServices = new ArrayList<>(0);

    public GitServiceRegistry(final ConfigurableApplicationContext applicationContext,
                              final GitRepository gitRepository,
                              final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers,
                              final boolean pushChanges,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners,
                              final List<GitRepositoryRegisteredServiceLocator> registeredServiceLocators) {
        super(applicationContext, serviceRegistryListeners);
        this.gitRepository = gitRepository;
        this.registeredServiceSerializers = registeredServiceSerializers;
        this.pushChanges = pushChanges;
        this.registeredServiceLocators = registeredServiceLocators;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            LOGGER.trace("Service id not set. Calculating id based on system time...");
            registeredService.setId(System.currentTimeMillis());
        }

        val message = "Saved changes to registered service " + registeredService.getName();
        val result = locateExistingRegisteredServiceFile(registeredService);
        result.ifPresentOrElse(file -> writeRegisteredServiceToFile(registeredService, file),
            () -> {
                val file = registeredServiceLocators.get(0).determine(registeredService,
                    GitRepositoryRegisteredServiceLocator.FILE_EXTENSIONS.get(0));
                writeRegisteredServiceToFile(registeredService, file);
            });

        invokeServiceRegistryListenerPreSave(registeredService);
        commitAndPush(message);
        load();
        return registeredService;
    }

    @SneakyThrows
    @Override
    public boolean delete(final RegisteredService registeredService) {
        val file = locateExistingRegisteredServiceFile(registeredService);
        if (file.isPresent()) {
            val message = "Deleted registered service " + registeredService.getName();
            FileUtils.forceDelete(file.get());
            commitAndPush(message);
            load();
            return true;
        }
        return false;
    }

    @Synchronized
    @Override
    public Collection<RegisteredService> load() {
        if (gitRepository.pull()) {
            LOGGER.debug("Successfully pulled changes from the remote repository");
        } else {
            LOGGER.warn("Unable to pull changes from the remote repository. Service definition files may be stale.");
        }

        val objects = this.gitRepository.getObjectsInRepository(
            new PathRegexPatternTreeFilter(GitRepositoryRegisteredServiceLocator.PATTERN_ACCEPTED_REPOSITORY_FILES));
        registeredServices = objects
            .stream()
            .filter(Objects::nonNull)
            .map(this::parseGitObjectContentIntoRegisteredService)
            .flatMap(Collection::stream)
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return registeredServices;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return this.registeredServices.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    private Optional<File> locateExistingRegisteredServiceFile(final RegisteredService registeredService) {
        return registeredServiceLocators.stream()
            .map(locator -> locator.locate(registeredService))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private void commitAndPush(final String message) {
        this.gitRepository.commitAll(message);
        if (this.pushChanges) {
            this.gitRepository.push();
        }
    }

    private List<RegisteredService> parseGitObjectContentIntoRegisteredService(final GitRepository.GitObject obj) {
        return this.registeredServiceSerializers
            .stream()
            .filter(s -> s.supports(obj.getContent()))
            .map(s -> s.load(new StringReader(obj.getContent())))
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }


    private boolean writeRegisteredServiceToFile(final RegisteredService registeredService, final File file) {
        try (val out = Files.newOutputStream(file.toPath())) {
            return this.registeredServiceSerializers.stream().anyMatch(s -> {
                s.to(out, registeredService);
                return true;
            });
        } catch (final IOException e) {
            throw new IllegalArgumentException("IO error opening file stream.", e);
        }
    }
}
