package org.jasig.cas.services;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.util.JsonSerializer;
import org.jasig.cas.util.LockedOutputStream;
import org.jasig.cas.util.services.RegisteredServiceJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import javax.annotation.PreDestroy;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Implementation of {@code ServiceRegistryDao} that reads services definition from JSON
 * configuration file at the Spring Application Context initialization time. JSON files are
 * expected to be found inside a directory location and this DAO will recursively look through
 * the directory structure to find relevant JSON files. Files are expected to have the
 * {@value #FILE_EXTENSION} extension. An example of the JSON file is included here:
 *
 * <pre>
 {
     "@class" : "org.jasig.cas.services.RegexRegisteredService",
     "id" : 103935657744185,
     "description" : "This is the application description",
     "serviceId" : "https://app.school.edu",
     "name" : "testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter",
     "theme" : "testtheme",
     "proxyPolicy" : {
        "@class" : "org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy",
        "pattern" : "https://.+"
     },
     "enabled" : true,
     "ssoEnabled" : false,
     "evaluationOrder" : 1000,
     "usernameAttributeProvider" : {
        "@class" : "org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider"
     },
     "logoutType" : "BACK_CHANNEL",
     "requiredHandlers" : [ "java.util.HashSet", [ "handler1", "handler2" ] ],
     "attributeReleasePolicy" : {
        "@class" : "org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy",
        "attributeFilter" : {
            "@class" : "org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter",
            "pattern" : "\\w+"
        },
        "allowedAttributes" : [ "java.util.ArrayList", [ "uid", "sn", "cn" ] ]
     }
 }
 * </pre>
 *
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Component("jsonServiceRegistryDao")
public class JsonServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceRegistryDao.class);

    /**
     * File extension of registered service JSON files.
     */
    private static final String FILE_EXTENSION = "json";

    /**
     * Map of service ID to registered service.
     */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    /**
     * The Service registry directory.
     */
    private Path serviceRegistryDirectory;

    /**
     * The Registered service json serializer.
     */
    private JsonSerializer<RegisteredService> registeredServiceJsonSerializer;

    @Autowired
    private ApplicationContext applicationContext;

    private Thread jsonServiceRegistryWatcherThread;
    private JsonServiceRegistryConfigWatcher jsonServiceRegistryConfigWatcher;

    /**
     * Instantiates a new Json service registry dao.
     *
     * @param configDirectory                 the config directory
     * @param registeredServiceJsonSerializer the registered service json serializer
     */
    public JsonServiceRegistryDao(final Path configDirectory, final JsonSerializer<RegisteredService> registeredServiceJsonSerializer) {
        initializeRegistry(configDirectory, registeredServiceJsonSerializer);
    }
    
    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link RegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     */
    public JsonServiceRegistryDao(final Path configDirectory) {
        this(configDirectory, new RegisteredServiceJsonSerializer());
    }

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link RegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @throws Exception the IO exception
     */
    @Autowired
    public JsonServiceRegistryDao(@Value("${service.registry.config.location:classpath:services}")
                                  final Resource configDirectory) throws Exception {
        
        if (configDirectory instanceof ClassPathResource) {
            final File servicesDirectory = prepareConfigDirectoryAsClasspathResource(configDirectory);
            initializeRegistry(Paths.get(servicesDirectory.getCanonicalFile().getCanonicalPath()), new RegisteredServiceJsonSerializer());
        } else {
            initializeRegistry(Paths.get(configDirectory.getFile().getCanonicalPath()), new RegisteredServiceJsonSerializer());
        }
    }

    private File prepareConfigDirectoryAsClasspathResource(final Resource configDirectory) throws IOException {
        final URL url = ResourceUtils.extractArchiveURL(configDirectory.getURL());
        final File file = ResourceUtils.getFile(url);

        final File servicesDirectory = new File(FileUtils.getTempDirectory(), configDirectory.getFilename());
        FileUtils.forceMkdir(servicesDirectory);
        FileUtils.cleanDirectory(servicesDirectory);

        final JarFile jFile = new JarFile(file);
        final Enumeration e = jFile.entries();
        while (e.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) e.nextElement();
            if (entry.getName().contains(configDirectory.getFilename()) && entry.getName().endsWith(FILE_EXTENSION)) {
                try (final InputStream stream = jFile.getInputStream(entry)) {
                    final File entryFileName = new File(entry.getName());
                    try (final FileWriter writer = new FileWriter(new File(servicesDirectory, entryFileName.getName()))) {
                        IOUtils.copy(stream, writer);
                    }
                }
            }
        }
        return servicesDirectory;
    }

    private void initializeRegistry(final Path configDirectory, final JsonSerializer<RegisteredService> registeredServiceJsonSerializer) {
        this.serviceRegistryDirectory = configDirectory;
        Assert.isTrue(this.serviceRegistryDirectory.toFile().exists(), serviceRegistryDirectory + " does not exist");
        Assert.isTrue(this.serviceRegistryDirectory.toFile().isDirectory(), serviceRegistryDirectory + " is not a directory");
        this.registeredServiceJsonSerializer = registeredServiceJsonSerializer;

        this.jsonServiceRegistryConfigWatcher = new JsonServiceRegistryConfigWatcher(this);
        this.jsonServiceRegistryWatcherThread = new Thread(this.jsonServiceRegistryConfigWatcher);
        this.jsonServiceRegistryWatcherThread.setName(this.getClass().getName());
        this.jsonServiceRegistryWatcherThread.start();
        LOGGER.debug("Started service registry watcher thread");
    }
    
    @Override
    public final RegisteredService save(final RegisteredService service) {
        if (service.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE && service instanceof AbstractRegisteredService) {
            LOGGER.debug("Service id not set. Calculating id based on system time...");
            ((AbstractRegisteredService) service).setId(System.nanoTime());
        }
        final File f = makeFile(service);
        try (final LockedOutputStream out = new LockedOutputStream(new FileOutputStream(f))) {
            this.registeredServiceJsonSerializer.toJson(out, service);

            if (this.serviceMap.containsKey(service.getId())) {
                LOGGER.debug("Found existing service definition by id [{}]. Saving...", service.getId());
            }
            this.serviceMap.put(service.getId(), service);
            LOGGER.debug("Saved service to [{}]", f.getCanonicalPath());
        } catch (final IOException e) {
            throw new RuntimeException("IO error opening file stream.", e);
        }
        return findServiceById(service.getId());
    }

    @Override
    public final synchronized boolean delete(final RegisteredService service) {
        try {
            final File f = makeFile(service);
            final boolean result = f.delete();
            if (!result) {
                LOGGER.warn("Failed to delete service definition file [{}]", f.getCanonicalPath());
            } else {
                serviceMap.remove(service.getId());
                LOGGER.debug("Successfully deleted service definition file [{}]", f.getCanonicalPath());
            }
            return result;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final synchronized List<RegisteredService> load() {
        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<>();
        final int[] errorCount = {0};
        final Collection<File> c = FileUtils.listFiles(this.serviceRegistryDirectory.toFile(), new String[] {FILE_EXTENSION}, true);
        c.stream().filter(file -> file.length() > 0).forEach(file -> {
            final RegisteredService service = loadRegisteredServiceFromFile(file);
            if (service == null) {
                LOGGER.warn("Could not load service definition from file {}", file);
                errorCount[0]++;
            } else {
                if (temp.containsKey(service.getId())) {
                    LOGGER.warn("Found a service definition [{}] with a duplicate id [{}]. "
                                    + "This will overwrite previous service definitions and is likely a "
                                    + "configuration problem. Make sure all services have a unique id and try again.",
                            service.getServiceId(), service.getId());
                }
                temp.put(service.getId(), service);
            }
        });

        if (errorCount[0] == 0) {
            this.serviceMap = temp;
        } else {
            LOGGER.warn("{} errors encountered when loading service definitions. New definitions are not loaded until errors are "
                   +  "corrected", errorCount[0]);
        }
        return new ArrayList(this.serviceMap.values());
    }

    @Override
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }

    /**
     * Load registered service from file.
     *
     * @param file the file
     * @return the registered service, or null if file cannot be read, is not found, is empty or parsing error occurs.
     */
    RegisteredService loadRegisteredServiceFromFile(final File file) {
        if (!file.canRead()) {
            LOGGER.warn("[{}] is not readable. Check file permissions", file.getName());
            return null;
        }

        if (!file.exists()) {
            LOGGER.warn("[{}] is not found at the path specified", file.getName());
            return null;
        }

        if (file.length() == 0) {
            LOGGER.debug("[{}] appears to be empty so no service definition will be loaded", file.getName());
            return null;
        }

        try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return this.registeredServiceJsonSerializer.fromJson(in);
        } catch (final Exception e) {
            LOGGER.error("Error reading configuration file " + file.getName(), e);
        }
        return null;
    }

    /**
     * Insert registered service into the existing map.
     *
     * @param service the service
     */
    void updateRegisteredService(final RegisteredService service) {
        this.serviceMap.put(service.getId(), service);
    }

    Path getServiceRegistryDirectory() {
        return serviceRegistryDirectory;
    }

    /**
     * Creates a JSON file for a registered service.
     * The file is named as {@code [SERVICE-NAME]-[SERVICE-ID]-.{@value #FILE_EXTENSION}}
     *
     * @param service Registered service.
     * @return JSON file in service registry directory.
     * @throws IllegalArgumentException if file name is invalid
     */
    protected File makeFile(final RegisteredService service) {
        final String fileName = StringUtils.remove(service.getName() + '-' + service.getId() + '.' + FILE_EXTENSION, " ");
        try {
            final File svcFile = new File(serviceRegistryDirectory.toFile(), fileName);
            LOGGER.debug("Using [{}] as the service definition file", svcFile.getCanonicalPath());
            return svcFile;
        } catch (final IOException e) {
            LOGGER.warn("Service file name {} is invalid; Examine for illegal characters in the name.", fileName);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Refreshes the services manager, forcing it to reload.
     */
    void refreshServicesManager() {
        if (this.applicationContext == null) {
            LOGGER.debug("Application context has failed to initialize because it's null. "
               + "Service definition may not take immediate effect, which suggests a configuration problem");
            return;
        }
        final ReloadableServicesManager manager = this.applicationContext.getBean(ReloadableServicesManager.class);
        if (manager != null) {
            manager.reload();
        } else {
            LOGGER.warn("Services manger could not be obtained from the application context. "
                + "Service definition may not take immediate effect, which suggests a configuration problem");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Destroy the watch service thread.
     */
    @PreDestroy
    public void destroy() {
        this.jsonServiceRegistryConfigWatcher.close();
        this.jsonServiceRegistryWatcherThread.interrupt();
    }
}
