package org.apereo.cas.services.resource;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ResourceBasedServiceRegistry;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.PathWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link AbstractResourceBasedServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
public abstract class AbstractResourceBasedServiceRegistry extends AbstractServiceRegistry
    implements ResourceBasedServiceRegistry, DisposableBean {
    /**
     * Fallback location to use if the given location is determined as invalid.
     */
    public static final File FALLBACK_REGISTERED_SERVICES_LOCATION =
        new File(CasConfigurationPropertiesSourceLocator.DEFAULT_CAS_CONFIG_DIRECTORIES.getFirst(), "services");


    @Getter
    protected Path serviceRegistryDirectory;
    
    @Getter
    protected Map<Long, RegisteredService> services = new ConcurrentHashMap<>();

    private final CasReentrantLock lock = new CasReentrantLock();

    private Collection<StringSerializer<RegisteredService>> registeredServiceSerializers;

    @Setter
    private WatcherService serviceRegistryWatcherService;

    private RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy;

    private RegisteredServiceResourceNamingStrategy resourceNamingStrategy;

    private Pattern serviceFileNamePattern;

    protected AbstractResourceBasedServiceRegistry(final Resource configDirectory,
                                                   final Collection<StringSerializer<RegisteredService>> serializers,
                                                   final ConfigurableApplicationContext applicationContext,
                                                   final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        this(configDirectory, serializers, applicationContext,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            serviceRegistryListeners, WatcherService.noOp());
    }

    protected AbstractResourceBasedServiceRegistry(final Resource configDirectory,
                                                   final Collection<StringSerializer<RegisteredService>> serializers,
                                                   final ConfigurableApplicationContext applicationContext,
                                                   final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                                   final WatcherService serviceRegistryConfigWatcher) {
        this(configDirectory, serializers, applicationContext,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            serviceRegistryListeners, serviceRegistryConfigWatcher);
    }


    protected AbstractResourceBasedServiceRegistry(final Path configDirectory,
                                                   final StringSerializer<RegisteredService> serializer,
                                                   final ConfigurableApplicationContext applicationContext,
                                                   final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                                   final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                                                   final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                                   final WatcherService serviceRegistryConfigWatcher) {
        this(configDirectory, CollectionUtils.wrap(serializer), applicationContext,
            registeredServiceReplicationStrategy, resourceNamingStrategy,
            serviceRegistryListeners, serviceRegistryConfigWatcher);
    }

    protected AbstractResourceBasedServiceRegistry(final Path configDirectory,
                                                   final Collection<StringSerializer<RegisteredService>> serializers,
                                                   final ConfigurableApplicationContext applicationContext,
                                                   final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                                   final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                                                   final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                                   final WatcherService serviceRegistryConfigWatcher) {
        super(applicationContext, serviceRegistryListeners);
        initializeRegistry(configDirectory, serializers,
            registeredServiceReplicationStrategy, resourceNamingStrategy, serviceRegistryConfigWatcher);
    }

    protected AbstractResourceBasedServiceRegistry(final Resource configDirectory,
                                                   final Collection<StringSerializer<RegisteredService>> serializers,
                                                   final ConfigurableApplicationContext applicationContext,
                                                   final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                                   final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                                                   final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                                   final WatcherService serviceRegistryConfigWatcher) {
        super(applicationContext, serviceRegistryListeners);
        LOGGER.trace("Provided service registry directory is specified at [{}]", configDirectory);

        FunctionUtils.doAndHandle(__ -> {
            val servicesDirectory = prepareRegisteredServicesDirectory(configDirectory);
            val file = servicesDirectory.getFile();
            LOGGER.trace("Prepared service registry directory is specified at [{}]", file);

            initializeRegistry(Paths.get(file.getCanonicalPath()), serializers,
                registeredServiceReplicationStrategy, resourceNamingStrategy, serviceRegistryConfigWatcher);
        });
    }

    private Resource prepareRegisteredServicesDirectory(final Resource configDirectory) throws IOException {
        val externalForm = configDirectory.getURI().toASCIIString();
        if (CasRuntimeHintsRegistrar.inNativeImage() && ResourceUtils.isEmbeddedResource(externalForm)) {
            val servicesDirectory = CasConfigurationPropertiesSourceLocator.DEFAULT_CAS_CONFIG_DIRECTORIES
                .stream()
                .map(directory -> new File(directory, "services"))
                .filter(File::exists)
                .findFirst()
                .orElse(FALLBACK_REGISTERED_SERVICES_LOCATION);
            LOGGER.warn("""
                GraalVM native image executable is unable to discover embedded resources at [{}]. The services directory location is changed to use [{}] instead. \
                To adjust this behavior, update your CAS settings to use a directory location outside the CAS native executable."""
                .stripIndent(), externalForm, servicesDirectory);
            return new FileSystemResource(servicesDirectory);
        }
        val pattern = String.join("|", getExtensions());
        return Objects.requireNonNull(ResourceUtils.prepareClasspathResourceIfNeeded(configDirectory, true, pattern),
            () -> "Could not determine the services configuration directory from " + configDirectory);
    }

    /**
     * Enable default watcher service.
     */
    public void enableDefaultWatcherService() {
        LOGGER.info("Watching service registry directory at [{}]", serviceRegistryDirectory);
        serviceRegistryWatcherService.close();
        val onCreate = new CreateResourceBasedRegisteredServiceWatcher(this);
        val onDelete = new DeleteResourceBasedRegisteredServiceWatcher(this);
        val onModify = new ModifyResourceBasedRegisteredServiceWatcher(this);
        serviceRegistryWatcherService = new PathWatcherService(serviceRegistryDirectory, onCreate, onModify, onDelete);
        serviceRegistryWatcherService.start(getClass().getSimpleName());
    }

    @Override
    public RegisteredService save(final RegisteredService service) {
        service.assignIdIfNecessary();
        val fileName = getRegisteredServiceFileName(service);
        try (val out = Files.newOutputStream(fileName.toPath())) {
            invokeServiceRegistryListenerPreSave(service);
            val result = registeredServiceSerializers.stream().anyMatch(serializer -> {
                try {
                    serializer.to(out, service);
                    return true;
                } catch (final Exception e) {
                    LOGGER.debug(e.getMessage(), e);
                    return false;
                }
            });
            if (!result) {
                throw new IOException("The service definition file could not be saved at " + fileName.getCanonicalPath());
            }
            if (this.services.containsKey(service.getId())) {
                LOGGER.debug("Found existing service definition by id [{}]. Saving...", service.getId());
            }
            services.put(service.getId(), service);
            LOGGER.debug("Saved service to [{}]", fileName.getCanonicalPath());
        } catch (final IOException e) {
            throw new IllegalArgumentException("IO error opening file stream.", e);
        }
        return findServiceById(service.getId());
    }

    @Override
    public boolean delete(final RegisteredService service) {
        return lock.tryLock(() -> FunctionUtils.doUnchecked(() -> {
            val filename = getRegisteredServiceFileName(service);
            val clientInfo = ClientInfoHolder.getClientInfo();
            publishEvent(new CasRegisteredServicePreDeleteEvent(this, service, clientInfo));
            val result = !filename.exists() || filename.delete();
            if (result) {
                removeRegisteredService(service);
                LOGGER.debug("Successfully deleted service definition file [{}]", filename.getCanonicalPath());
            } else {
                LOGGER.warn("Failed to delete service definition file [{}]", filename.getCanonicalPath());
            }
            publishEvent(new CasRegisteredServiceDeletedEvent(this, service, clientInfo));
            return result;
        }));
    }

    @Override
    public void deleteAll() {
        val files = FileUtils.listFiles(this.serviceRegistryDirectory.toFile(), getExtensions(), true);
        files.forEach(File::delete);
    }

    @Override
    public Collection<RegisteredService> load() {
        return lock.tryLock(() -> {
            LOGGER.trace("Loading files from [{}]", this.serviceRegistryDirectory);
            val serviceRegistryDirectoryFile = serviceRegistryDirectory.toFile();
            val files = serviceRegistryDirectoryFile.exists()
                ? FileUtils.listFiles(serviceRegistryDirectoryFile, getExtensions(), true)
                : List.<File>of();
            
            LOGGER.trace("Located [{}] files from [{}] are [{}]", getExtensions(), this.serviceRegistryDirectory, files);
            val clientInfo = ClientInfoHolder.getClientInfo();

            this.services = files
                .stream()
                .map(this::load)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(service -> StringUtils.isNotBlank(service.getServiceId()) && StringUtils.isNotBlank(service.getName()))
                .sorted()
                .collect(Collectors.toMap(RegisteredService::getId, Function.identity(),
                    (s1, s2) -> {
                        BaseResourceBasedRegisteredServiceWatcher.LOG_SERVICE_DUPLICATE.accept(s2);
                        return s1;
                    }, LinkedHashMap::new));
            val listedServices = new ArrayList<>(this.services.values());
            val results = registeredServiceReplicationStrategy.updateLoadedRegisteredServicesFromCache(listedServices, this);
            results.forEach(service -> publishEvent(new CasRegisteredServiceLoadedEvent(this, service, clientInfo)));
            return results;
        });
    }

    @Override
    public Collection<RegisteredService> load(final File file) {
        val fileName = file.getName();
        if (!file.canRead()) {
            LOGGER.warn("[{}] is not readable. Check file permissions", fileName);
            return new ArrayList<>();
        }
        if (!file.exists()) {
            LOGGER.warn("[{}] is not found at the path specified", fileName);
            return new ArrayList<>();
        }
        if (file.length() == 0) {
            LOGGER.debug("[{}] appears to be empty so no service definition will be loaded", fileName);
            return new ArrayList<>();
        }
        if (!fileName.isEmpty() && fileName.charAt(0) == '.') {
            LOGGER.debug("[{}] starts with ., ignoring", fileName);
            return new ArrayList<>();
        }
        if (Arrays.stream(getExtensions()).noneMatch(fileName::endsWith)) {
            LOGGER.debug("[{}] doesn't end with valid extension, ignoring", fileName);
            return new ArrayList<>();
        }

        if (!RegexUtils.matches(this.serviceFileNamePattern, fileName)) {
            LOGGER.warn("[{}] does not match the recommended pattern [{}]. "
                    + "While CAS tries to be forgiving as much as possible, it's recommended "
                    + "that you rename the file to match the requested pattern to avoid issues with duplicate service loading. "
                    + "Future CAS versions may try to strictly force the naming syntax, refusing to load the file.",
                fileName, this.serviceFileNamePattern.pattern());
        }

        LOGGER.debug("Attempting to read and parse [{}]", file.getAbsoluteFile());
        try (val in = Files.newBufferedReader(file.toPath())) {
            return registeredServiceSerializers
                .stream()
                .filter(serializer -> serializer.supports(file))
                .map(serializer -> serializer.load(in))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(service -> StringUtils.isNotBlank(service.getServiceId()) && StringUtils.isNotBlank(service.getName()))
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error("Error reading configuration file [{}]", fileName);
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>();
    }

    @Override
    public Stream<? extends RegisteredService> getServicesStream() {
        return this.services.values().stream();
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val service = this.services.get(id);
        return this.registeredServiceReplicationStrategy.getRegisteredServiceFromCacheIfAny(service, id, this);
    }

    @Override
    public long size() {
        return this.services.size();
    }

    @Override
    public void update(final RegisteredService service) {
        this.services.put(service.getId(), service);
    }

    @Override
    public void destroy() {
        this.serviceRegistryWatcherService.close();
    }

    protected void removeRegisteredService(final RegisteredService service) {
        this.services.remove(service.getId());
    }

    protected RegisteredService getRegisteredServiceFromFile(final File file) {
        val fileName = file.getName();
        if (!fileName.isEmpty() && fileName.charAt(0) == '.') {
            LOGGER.trace("[{}] starts with ., ignoring...", fileName);
            return null;
        }
        if (Arrays.stream(getExtensions()).noneMatch(fileName::endsWith)) {
            LOGGER.trace("[{}] doesn't end with valid extension, ignoring", fileName);
            return null;
        }
        val matcher = this.serviceFileNamePattern.matcher(fileName);
        if (matcher.find()) {
            val serviceId = matcher.group(2);
            if (NumberUtils.isCreatable(serviceId)) {
                val id = Long.parseLong(serviceId);
                return findServiceById(id);
            }
            val serviceName = matcher.group(1);
            return findServiceByExactServiceName(serviceName);
        }
        LOGGER.warn("Provided file [{}] does not match the recommended service definition file pattern [{}]",
            file.getName(),
            this.serviceFileNamePattern.pattern());
        return null;
    }

    protected File getRegisteredServiceFileName(final RegisteredService service) {
        val fileName = resourceNamingStrategy.build(service, getExtensions()[0]);

        val parentDirectory = determineParentDirectoryFor(service);
        val svcFile = new File(parentDirectory, fileName);
        LOGGER.debug("Using [{}] as the service definition file", svcFile.getAbsolutePath());
        return svcFile;
    }

    private File determineParentDirectoryFor(final RegisteredService service) {
        val defaultServicesDirectory = serviceRegistryDirectory.toFile();

        val friendlyName = service.getFriendlyName();
        val candidateParentDirectories = List.of(
            new File(defaultServicesDirectory, friendlyName.toLowerCase(Locale.ENGLISH).replace(" ", "-")),
            new File(defaultServicesDirectory, friendlyName)
        );
        return candidateParentDirectories
            .stream()
            .filter(dir -> dir.exists() && dir.isDirectory())
            .findFirst()
            .orElse(defaultServicesDirectory);
    }

    /**
     * Gets extension associated with files in the given resource directory.
     *
     * @return the extension
     */
    protected abstract String[] getExtensions();

    private void initializeRegistry(final Path configDirectory,
                                    final Collection<StringSerializer<RegisteredService>> serializers,
                                    final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                    final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                                    final WatcherService serviceRegistryConfigWatcher) {
        this.registeredServiceReplicationStrategy = ObjectUtils.getIfNull(registeredServiceReplicationStrategy,
            new NoOpRegisteredServiceReplicationStrategy());
        this.resourceNamingStrategy = ObjectUtils.getIfNull(resourceNamingStrategy, new DefaultRegisteredServiceResourceNamingStrategy());
        this.registeredServiceSerializers = serializers;

        this.serviceFileNamePattern = resourceNamingStrategy.buildNamingPattern(getExtensions());
        LOGGER.trace("Constructed service name file pattern [{}]", serviceFileNamePattern.pattern());

        this.serviceRegistryDirectory = configDirectory;
        val file = this.serviceRegistryDirectory.toFile();
        Assert.isTrue(file.exists(), this.serviceRegistryDirectory + " does not exist");
        Assert.isTrue(file.isDirectory(), this.serviceRegistryDirectory + " is not a directory");
        LOGGER.trace("Service registry directory is specified at [{}]", file);

        this.serviceRegistryWatcherService = serviceRegistryConfigWatcher;
        this.serviceRegistryWatcherService.start(getClass().getSimpleName());
    }

}
