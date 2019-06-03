package org.apereo.cas.services.resource;

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
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.PathWatcherService;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractResourceBasedServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
public abstract class AbstractResourceBasedServiceRegistry extends AbstractServiceRegistry implements ResourceBasedServiceRegistry, DisposableBean {

    private static final BinaryOperator<RegisteredService> LOG_DUPLICATE_AND_RETURN_FIRST_ONE = (s1, s2) -> {
        BaseResourceBasedRegisteredServiceWatcher.LOG_SERVICE_DUPLICATE.accept(s2);
        return s1;
    };

    /**
     * The Service registry directory.
     */
    @Getter
    protected Path serviceRegistryDirectory;

    /**
     * Map of service ID to registered service.
     */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    /**
     * The Registered service json serializers.
     */
    private Collection<StringSerializer<RegisteredService>> registeredServiceSerializers;

    private PathWatcherService serviceRegistryConfigWatcher;

    private RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy;

    private RegisteredServiceResourceNamingStrategy resourceNamingStrategy;

    private Pattern serviceFileNamePattern;

    public AbstractResourceBasedServiceRegistry(final Resource configDirectory,
                                                final Collection<StringSerializer<RegisteredService>> serializers,
                                                final ApplicationEventPublisher eventPublisher,
                                                final Collection<ServiceRegistryListener> serviceRegistryListeners) throws Exception {
        this(configDirectory, serializers, false, eventPublisher,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            serviceRegistryListeners);
    }

    public AbstractResourceBasedServiceRegistry(final Path configDirectory, final StringSerializer<RegisteredService> serializer,
                                                final boolean enableWatcher, final ApplicationEventPublisher eventPublisher,
                                                final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                                final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                                                final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        this(configDirectory, CollectionUtils.wrap(serializer), enableWatcher, eventPublisher,
            registeredServiceReplicationStrategy, resourceNamingStrategy,
            serviceRegistryListeners);
    }

    public AbstractResourceBasedServiceRegistry(final Path configDirectory,
                                                final Collection<StringSerializer<RegisteredService>> serializers,
                                                final boolean enableWatcher,
                                                final ApplicationEventPublisher eventPublisher,
                                                final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                                final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                                                final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(eventPublisher, serviceRegistryListeners);
        initializeRegistry(configDirectory, serializers, enableWatcher, registeredServiceReplicationStrategy, resourceNamingStrategy);
    }

    public AbstractResourceBasedServiceRegistry(final Resource configDirectory,
                                                final Collection<StringSerializer<RegisteredService>> serializers,
                                                final boolean enableWatcher,
                                                final ApplicationEventPublisher eventPublisher,
                                                final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                                final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                                                final Collection<ServiceRegistryListener> serviceRegistryListeners) throws Exception {
        super(eventPublisher, serviceRegistryListeners);
        LOGGER.trace("Provided service registry directory is specified at [{}]", configDirectory);
        val pattern = String.join("|", getExtensions());
        val servicesDirectory = ResourceUtils.prepareClasspathResourceIfNeeded(configDirectory, true, pattern);
        if (servicesDirectory == null) {
            throw new IllegalArgumentException("Could not determine the services configuration directory from " + configDirectory);
        }
        val file = servicesDirectory.getFile();
        LOGGER.trace("Prepared service registry directory is specified at [{}]", file);

        initializeRegistry(Paths.get(file.getCanonicalPath()), serializers, enableWatcher,
            registeredServiceReplicationStrategy, resourceNamingStrategy);
    }

    private void initializeRegistry(final Path configDirectory, final Collection<StringSerializer<RegisteredService>> serializers,
                                    final boolean enableWatcher,
                                    final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                                    final RegisteredServiceResourceNamingStrategy resourceNamingStrategy) {
        this.registeredServiceReplicationStrategy = ObjectUtils.defaultIfNull(registeredServiceReplicationStrategy,
            new NoOpRegisteredServiceReplicationStrategy());
        this.resourceNamingStrategy = ObjectUtils.defaultIfNull(resourceNamingStrategy, new DefaultRegisteredServiceResourceNamingStrategy());
        this.registeredServiceSerializers = serializers;

        this.serviceFileNamePattern = resourceNamingStrategy.buildNamingPattern(getExtensions());
        LOGGER.trace("Constructed service name file pattern [{}]", serviceFileNamePattern.pattern());


        this.serviceRegistryDirectory = configDirectory;
        val file = this.serviceRegistryDirectory.toFile();
        Assert.isTrue(file.exists(), this.serviceRegistryDirectory + " does not exist");
        Assert.isTrue(file.isDirectory(), this.serviceRegistryDirectory + " is not a directory");
        LOGGER.trace("Service registry directory is specified at [{}]", file);
        if (enableWatcher) {
            enableServicesDirectoryPathWatcher();
        }
    }

    private void enableServicesDirectoryPathWatcher() {
        LOGGER.info("Watching service registry directory at [{}]", this.serviceRegistryDirectory);
        val onCreate = new CreateResourceBasedRegisteredServiceWatcher(this);
        val onDelete = new DeleteResourceBasedRegisteredServiceWatcher(this);
        val onModify = new ModifyResourceBasedRegisteredServiceWatcher(this);
        this.serviceRegistryConfigWatcher = new PathWatcherService(this.serviceRegistryDirectory, onCreate, onModify, onDelete);
        this.serviceRegistryConfigWatcher.start(getClass().getSimpleName());
        LOGGER.debug("Started service registry watcher thread");
    }

    /**
     * Destroy the watch service thread.
     */
    @Override
    public void destroy() {
        if (this.serviceRegistryConfigWatcher != null) {
            this.serviceRegistryConfigWatcher.close();
        }
    }

    @Override
    public long size() {
        return this.serviceMap.size();
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val service = this.serviceMap.get(id);
        return this.registeredServiceReplicationStrategy.getRegisteredServiceFromCacheIfAny(service, id, this);
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        val service = this.serviceMap.values().stream().filter(r -> r.matches(id)).findFirst().orElse(null);
        return this.registeredServiceReplicationStrategy.getRegisteredServiceFromCacheIfAny(service, id, this);
    }

    @Override
    @SneakyThrows
    public synchronized boolean delete(final RegisteredService service) {

        val f = getRegisteredServiceFileName(service);
        publishEvent(new CasRegisteredServicePreDeleteEvent(this, service));
        val result = !f.exists() || f.delete();
        if (!result) {
            LOGGER.warn("Failed to delete service definition file [{}]", f.getCanonicalPath());
        } else {
            removeRegisteredService(service);
            LOGGER.debug("Successfully deleted service definition file [{}]", f.getCanonicalPath());
        }
        publishEvent(new CasRegisteredServiceDeletedEvent(this, service));
        return result;
    }

    /**
     * Remove registered service.
     *
     * @param service the service
     */
    protected void removeRegisteredService(final RegisteredService service) {
        this.serviceMap.remove(service.getId());
    }

    @Override
    public synchronized Collection<RegisteredService> load() {
        LOGGER.trace("Loading files from [{}]", this.serviceRegistryDirectory);
        val files = FileUtils.listFiles(this.serviceRegistryDirectory.toFile(), getExtensions(), true);
        LOGGER.trace("Located [{}] files from [{}] are [{}]", getExtensions(), this.serviceRegistryDirectory, files);

        this.serviceMap = files
            .stream()
            .map(this::load)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .sorted()
            .collect(Collectors.toMap(RegisteredService::getId, Function.identity(),
                LOG_DUPLICATE_AND_RETURN_FIRST_ONE, LinkedHashMap::new));
        val services = new ArrayList<RegisteredService>(this.serviceMap.values());
        val results = this.registeredServiceReplicationStrategy.updateLoadedRegisteredServicesFromCache(services, this);
        results.forEach(service -> publishEvent(new CasRegisteredServiceLoadedEvent(this, service)));
        return results;
    }

    @Override
    @SneakyThrows
    public Collection<RegisteredService> load(final File file) {
        val fileName = file.getName();
        if (!file.canRead()) {
            LOGGER.warn("[{}] is not readable. Check file permissions", fileName);
            return new ArrayList<>(0);
        }
        if (!file.exists()) {
            LOGGER.warn("[{}] is not found at the path specified", fileName);
            return new ArrayList<>(0);
        }
        if (file.length() == 0) {
            LOGGER.debug("[{}] appears to be empty so no service definition will be loaded", fileName);
            return new ArrayList<>(0);
        }
        if (fileName.startsWith(".")) {
            LOGGER.debug("[{}] starts with ., ignoring", fileName);
            return new ArrayList<>(0);
        }
        if (Arrays.stream(getExtensions()).noneMatch(fileName::endsWith)) {
            LOGGER.debug("[{}] doesn't end with valid extension, ignoring", fileName);
            return new ArrayList<>(0);
        }

        if (!RegexUtils.matches(this.serviceFileNamePattern, fileName)) {
            LOGGER.warn("[{}] does not match the recommended pattern [{}]. "
                    + "While CAS tries to be forgiving as much as possible, it's recommended "
                    + "that you rename the file to match the requested pattern to avoid issues with duplicate service loading. "
                    + "Future CAS versions may try to strictly force the naming syntax, refusing to load the file.",
                fileName, this.serviceFileNamePattern.pattern());
        }

        LOGGER.trace("Attempting to read and parse [{}]", file.getCanonicalFile());
        try (val in = Files.newBufferedReader(file.toPath())) {
            return this.registeredServiceSerializers
                .stream()
                .filter(s -> s.supports(file))
                .map(s -> s.load(in))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error("Error reading configuration file [{}]", fileName, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public RegisteredService save(final RegisteredService service) {
        if (service.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            LOGGER.debug("Service id not set. Calculating id based on system time...");
            service.setId(System.currentTimeMillis());
        }
        val f = getRegisteredServiceFileName(service);
        try (val out = Files.newOutputStream(f.toPath())) {
            invokeServiceRegistryListenerPreSave(service);
            val result = this.registeredServiceSerializers.stream().anyMatch(s -> {
                try {
                    s.to(out, service);
                    return true;
                } catch (final Exception e) {
                    LOGGER.debug(e.getMessage(), e);
                    return false;
                }
            });
            if (!result) {
                throw new IOException("The service definition file could not be saved at " + f.getCanonicalPath());
            }
            if (this.serviceMap.containsKey(service.getId())) {
                LOGGER.debug("Found existing service definition by id [{}]. Saving...", service.getId());
            }
            this.serviceMap.put(service.getId(), service);
            LOGGER.debug("Saved service to [{}]", f.getCanonicalPath());
        } catch (final IOException e) {
            throw new IllegalArgumentException("IO error opening file stream.", e);
        }
        return findServiceById(service.getId());
    }

    @Override
    public void update(final RegisteredService service) {
        this.serviceMap.put(service.getId(), service);
    }

    /**
     * Gets registered service from file.
     *
     * @param file the file
     * @return the registered service from file
     */
    protected RegisteredService getRegisteredServiceFromFile(final File file) {
        val fileName = file.getName();
        if (fileName.startsWith(".")) {
            LOGGER.trace("[{}] starts with ., ignoring", fileName);
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

    /**
     * Creates a file for a registered service.
     * The file is named as {@code [SERVICE-NAME]-[SERVICE-ID]-.{@value #getExtensions()}}
     *
     * @param service Registered service.
     * @return file in service registry directory.
     * @throws IllegalArgumentException if file name is invalid
     */
    @SneakyThrows
    protected File getRegisteredServiceFileName(final RegisteredService service) {
        val fileName = resourceNamingStrategy.build(service, getExtensions()[0]);
        val svcFile = new File(this.serviceRegistryDirectory.toFile(), fileName);
        LOGGER.debug("Using [{}] as the service definition file", svcFile.getCanonicalPath());
        return svcFile;

    }

    /**
     * Gets extension associated with files in the given resource directory.
     *
     * @return the extension
     */
    protected abstract String[] getExtensions();
}
