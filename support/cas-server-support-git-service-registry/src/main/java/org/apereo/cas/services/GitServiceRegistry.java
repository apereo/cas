package org.apereo.cas.services;

import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.PathRegexPatternTreeFilter;
import org.apereo.cas.services.locator.GitRepositoryRegisteredServiceLocator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
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
    private final CasReentrantLock lock = new CasReentrantLock();

    private final GitRepository gitRepository;

    private final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers;

    private final boolean pushChanges;

    private final List<GitRepositoryRegisteredServiceLocator> registeredServiceLocators;

    private final String rootDirectory;

    private Collection<RegisteredService> registeredServices = new ArrayList<>();

    public GitServiceRegistry(final ConfigurableApplicationContext applicationContext,
                              final GitRepository gitRepository,
                              final Collection<StringSerializer<RegisteredService>> registeredServiceSerializers,
                              final boolean pushChanges,
                              final String rootDirectory,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners,
                              final List<GitRepositoryRegisteredServiceLocator> registeredServiceLocators) {
        super(applicationContext, serviceRegistryListeners);
        this.gitRepository = gitRepository;
        this.registeredServiceSerializers = registeredServiceSerializers;
        this.pushChanges = pushChanges;
        this.registeredServiceLocators = registeredServiceLocators;
        this.rootDirectory = rootDirectory;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return Unchecked.supplier(() -> {
            registeredService.assignIdIfNecessary();

            val message = "Saved changes to registered service " + registeredService.getName();
            val result = locateExistingRegisteredServiceFile(registeredService);
            result.ifPresentOrElse(file -> writeRegisteredServiceToFile(registeredService, file),
                () -> {
                    val file = registeredServiceLocators.getFirst().determine(registeredService,
                        GitRepositoryRegisteredServiceLocator.FILE_EXTENSIONS.getFirst());
                    writeRegisteredServiceToFile(registeredService, file);
                });

            invokeServiceRegistryListenerPreSave(registeredService);
            commitAndPush(message);
            load();
            return registeredService;
        }).get();
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val file = locateExistingRegisteredServiceFile(registeredService);
        if (file.isPresent()) {
            val message = "Deleted registered service " + registeredService.getName();
            FunctionUtils.doUnchecked(__ -> {
                FileUtils.forceDelete(file.get());
                commitAndPush(message);
            });
            load();
            return true;
        }
        return false;
    }

    @Override
    public void deleteAll() {
        FunctionUtils.doUnchecked(__ -> {
            val currentServices = load();
            currentServices.stream()
                .map(this::locateExistingRegisteredServiceFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(Unchecked.consumer(FileUtils::forceDelete));
            val message = "Deleted registered services from repository";
            commitAndPush(message);
        });
    }

    @Override
    public Collection<RegisteredService> load() {
        return lock.tryLock(() -> {
            try {
                if (gitRepository.pull()) {
                    LOGGER.debug("Successfully pulled changes from the remote repository");
                } else {
                    LOGGER.info("Unable to pull changes from the remote repository. Service definition files may be stale.");
                }
                val objectPatternStr = StringUtils.isBlank(rootDirectory)
                    ? GitRepositoryRegisteredServiceLocator.PATTEN_ACCEPTED_REPOSITORY_FILES
                    : rootDirectory + '/' + GitRepositoryRegisteredServiceLocator.PATTEN_ACCEPTED_REPOSITORY_FILES;
                val objectPattern = RegexUtils.createPattern(objectPatternStr, Pattern.CASE_INSENSITIVE);
                val objects = gitRepository.getObjectsInRepository(
                    new PathRegexPatternTreeFilter(objectPattern));
                registeredServices = objects
                    .stream()
                    .filter(Objects::nonNull)
                    .map(this::parseGitObjectContentIntoRegisteredService)
                    .flatMap(Collection::stream)
                    .map(this::invokeServiceRegistryListenerPostLoad)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                return registeredServices;
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER, e);
                val parentDir = StringUtils.isBlank(rootDirectory)
                    ? gitRepository.getRepositoryDirectory()
                    : new File(gitRepository.getRepositoryDirectory(), rootDirectory);
                val files = FileUtils.listFiles(parentDir,
                    GitRepositoryRegisteredServiceLocator.FILE_EXTENSIONS.toArray(ArrayUtils.EMPTY_STRING_ARRAY), true);
                LOGGER.debug("Located [{}] files(s)", files.size());

                registeredServices = files
                    .stream()
                    .filter(file -> file.isFile() && file.canRead() && file.canWrite() && file.length() > 0)
                    .map(Unchecked.function(file -> {
                        try (val in = Files.newBufferedReader(file.toPath())) {
                            return registeredServiceSerializers
                                .stream()
                                .filter(s -> s.supports(file))
                                .map(s -> s.load(in))
                                .filter(Objects::nonNull)
                                .flatMap(Collection::stream)
                                .map(this::invokeServiceRegistryListenerPostLoad)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        } catch (final Exception ex) {
                            LOGGER.error("Error reading configuration file [{}]", file.toPath());
                            LoggingUtils.error(LOGGER, ex);
                        }
                        return new ArrayList<RegisteredService>();
                    }))
                    .flatMap(List::stream)
                    .sorted()
                    .map(this::invokeServiceRegistryListenerPostLoad)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                return registeredServices;
            }
        });
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

    private void commitAndPush(final String message) throws Exception {
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
