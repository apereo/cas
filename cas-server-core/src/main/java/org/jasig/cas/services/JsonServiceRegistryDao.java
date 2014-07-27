/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.services;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jasig.cas.util.LockedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implementation of <code>ServiceRegistryDao</code> that reads services definition from JSON
 * configuration file at the Spring Application Context initialization time.
 *
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class JsonServiceRegistryDao implements ServiceRegistryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceRegistryDao.class);

    /** File extension of registered service JSON files. */
    private static final String FILE_EXTENSION = ".json";

    /** Service id field for a given registered service. */
    private static final String SERVICE_ID_KEY = "serviceId";

    /** Map of service ID to registered service. */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<Long, RegisteredService>();

    /** Regex pattern identifier for the service id. */
    private static final String REGEX_PREFIX = "^";

    private final ObjectMapper objectMapper;

    private final File serviceRegistryDirectory;

    private final PrettyPrinter prettyPrinter;

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored.
     *
     * @param configDirectory the config directory
     * @param prettyPrinter the pretty printer
     */
    public JsonServiceRegistryDao(final File configDirectory, final PrettyPrinter prettyPrinter) {
        this.serviceRegistryDirectory = configDirectory;
        Assert.isTrue(this.serviceRegistryDirectory.exists(), serviceRegistryDirectory + " does not exist");
        Assert.isTrue(this.serviceRegistryDirectory.isDirectory(), serviceRegistryDirectory + " is not a directory");

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        this.objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);

        this.objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        this.prettyPrinter = prettyPrinter;
    }

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored.
     *
     * @param configDirectory the config directory
     */
    public JsonServiceRegistryDao(final File configDirectory) {
        this(configDirectory, new DefaultPrettyPrinter());
    }

    @Override
    public RegisteredService save(final RegisteredService service) {
        if (service.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE && service instanceof AbstractRegisteredService) {
            ((AbstractRegisteredService) service).setId(System.nanoTime());
        }
        LockedOutputStream out = null;
        try {
            final File f = makeFile(service);
            out = new LockedOutputStream(new FileOutputStream(f));
            this.objectMapper.writer(this.prettyPrinter).writeValue(out, service);
            LOGGER.debug("Saved service to [{}]", f.getCanonicalPath());
        } catch (final IOException e) {
            throw new RuntimeException("IO error opening file stream.", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        load();
        return findServiceById(service.getId());
    }

    @Override
    public synchronized boolean delete(final RegisteredService service) {
        serviceMap.remove(service.getId());
        final boolean result = makeFile(service).delete();
        load();
        return result;
    }

    @Override
    public synchronized List<RegisteredService> load() {
        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<Long, RegisteredService>();
        final FilenameFilter filter = new SuffixFileFilter(FILE_EXTENSION);

        int errorCount = 0;

        for (final File file : this.serviceRegistryDirectory.listFiles(filter)) {
            BufferedInputStream in = null;
            try {
                if (file.length() > 0) {
                    in = new BufferedInputStream(new FileInputStream(file));
                    Object oo = this.objectMapper.readValue(in, Object.class);
                    System.out.print(oo);

                    final Map<?, ?> record = null;

                    final String serviceId = record.get(SERVICE_ID_KEY).toString();

                    final Class<? extends RegisteredService> clazz = getRegisteredServiceInstance(serviceId);
                    final RegisteredService service = this.objectMapper.convertValue(record, clazz);

                    temp.put(service.getId(), service);
                }
            } catch (final Exception e) {
                errorCount++;
                LOGGER.error("Error reading {}", file, e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        if (errorCount == 0) {
            this.serviceMap = temp;
        }

        return new ArrayList<RegisteredService>(this.serviceMap.values());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }

    /**
     * Creates a JSON file for a registered service.
     *
     * @param  service  Registered service.
     * @return  JSON file in service registry directory.
     */
    protected File makeFile(final RegisteredService service) {
        return new File(serviceRegistryDirectory, service.getName() + FILE_EXTENSION);
    }

    /**
     * Determine the validity of a regexx pattern.
     * Patterns must begin with {@link #REGEX_PREFIX}
     * and must also be able to compile correctly.
     *
     * @param pattern the pattern
     * @return true, if regex
     */
    private boolean isValidRegexPattern(final String pattern) {
        boolean valid = false;
        try {
            if (pattern.startsWith(REGEX_PREFIX)) {
                Pattern.compile(pattern);
                valid = true;
            }
        } catch (final PatternSyntaxException e) {
            LOGGER.debug("Failed to identify [{}] as a regular expression", pattern);
        }
        return valid;
    }

    /**
     * Constructs an instance of {@link RegisteredService} based on the
     * syntax of the pattern defined.
     *
     * @param pattern the pattern of the service definition
     * @return an instance of {@link #isValidRegexPattern(String)}
     * @see #isValidRegexPattern(String)
     */
    private Class<? extends RegisteredService> getRegisteredServiceInstance(final String pattern) {
        if (isValidRegexPattern(pattern)) {
            return RegexRegisteredService.class;
        }

        return RegisteredServiceImpl.class;
    }
}
