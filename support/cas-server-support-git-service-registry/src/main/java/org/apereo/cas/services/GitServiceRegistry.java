package org.apereo.cas.services;

import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.PathRegexPatternTreeFilter;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link GitServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GitServiceRegistry extends AbstractServiceRegistry {
    private static final List<String> FILE_EXTENSIONS = CollectionUtils.wrapList("json", "yaml", "yml");
    private static final Pattern PATTERN_ACCEPTED_REPOSITORY_FILES = RegexUtils.createPattern(".+\\.("
        + String.join("|", FILE_EXTENSIONS) + ')', Pattern.CASE_INSENSITIVE);

    private final Collection<RegisteredService> registeredServices = new ArrayList<>();

    private final GitRepository gitRepository;
    private final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers;
    private final RegisteredServiceResourceNamingStrategy resourceNamingStrategy;
    private final boolean pushChanges;

    public GitServiceRegistry(final ApplicationEventPublisher eventPublisher,
                              final GitRepository gitRepository,
                              final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers,
                              final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                              final boolean pushChanges,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(eventPublisher, serviceRegistryListeners);
        this.gitRepository = gitRepository;
        this.registeredServiceSerializers = registeredServiceSerializers;
        this.resourceNamingStrategy = resourceNamingStrategy;
        this.pushChanges = pushChanges;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            LOGGER.debug("Service id not set. Calculating id based on system time...");
            registeredService.setId(System.currentTimeMillis());
        }

        val message = "Saved changes to registered service " + registeredService.getName();
        val result = getRegisteredServiceFileName(registeredService);
        result.ifPresentOrElse(file -> writeRegisteredServiceToFile(registeredService, file),
            () -> {
                val fileName = resourceNamingStrategy.build(registeredService, FILE_EXTENSIONS.get(0));
                val file = new File(gitRepository.getRepositoryDirectory(), fileName);
                writeRegisteredServiceToFile(registeredService, file);
            });

        invokeServiceRegistryListenerPreSave(registeredService);
        this.gitRepository.commitAll(message);
        if (this.pushChanges) {
            this.gitRepository.push();
        }
        load();
        return registeredService;
    }

    @SneakyThrows
    @Override
    public boolean delete(final RegisteredService registeredService) {
        val file = getRegisteredServiceFileName(registeredService);
        if (file.isPresent()) {
            val message = "Deleted registered service " + registeredService.getName();
            FileUtils.forceDelete(file.get());
            this.gitRepository.commitAll(message);
            if (this.pushChanges) {
                this.gitRepository.push();
            }
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

        val objects = this.gitRepository.getObjectsInRepository(new PathRegexPatternTreeFilter(PATTERN_ACCEPTED_REPOSITORY_FILES));
        registeredServices.clear();
        registeredServices.addAll(objects
            .stream()
            .filter(Objects::nonNull)
            .map(this::parseGitObjectContentIntoRegisteredService)
            .flatMap(Collection::stream)
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
        return registeredServices;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return this.registeredServices.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return this.registeredServices.stream().filter(r -> r.matches(id)).findFirst().orElse(null);
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

    @SneakyThrows
    private Optional<File> getRegisteredServiceFileName(final RegisteredService service) {
        return FILE_EXTENSIONS.stream()
            .map(ext -> {
                val fileName = resourceNamingStrategy.build(service, ext);
                return new File(gitRepository.getRepositoryDirectory(), fileName);
            })
            .filter(File::exists)
            .findFirst();
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
