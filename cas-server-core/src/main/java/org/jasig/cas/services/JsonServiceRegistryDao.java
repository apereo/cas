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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jasig.cas.util.LockedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of <code>ServiceRegistryDao</code> that reads services definition from JSON
 * configuration file at the Spring Application Context initialization time.
 *
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1
 */
public class JsonServiceRegistryDao implements ServiceRegistryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceRegistryDao.class);

    /** File extension of registered service JSON files. */
    private static final String FILE_EXTENSION = "json";

    /** Map of service ID to registered service. */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<Long, RegisteredService>();

    /**
     * The Object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * The Service registry directory.
     */
    private final File serviceRegistryDirectory;

    /**
     * The Pretty printer.
     */
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
        this(configDirectory, initializeObjectMapper(), prettyPrinter);
    }

    /**
     * Instantiates a new Json service registry dao.
     *
     * @param configDirectory the config directory
     * @param objectMapper the object mapper
     * @param prettyPrinter the pretty printer
     */
    public JsonServiceRegistryDao(final File configDirectory, final ObjectMapper objectMapper, final PrettyPrinter prettyPrinter) {
        this.serviceRegistryDirectory = configDirectory;
        Assert.isTrue(this.serviceRegistryDirectory.exists(), serviceRegistryDirectory + " does not exist");
        Assert.isTrue(this.serviceRegistryDirectory.isDirectory(), serviceRegistryDirectory + " is not a directory");

        this.objectMapper = objectMapper;
        this.prettyPrinter = prettyPrinter;
    }

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored.
     *
     * @param configDirectory the config directory where service registry files can be found.
     */
    public JsonServiceRegistryDao(final File configDirectory) {
        this(configDirectory, new DefaultPrettyPrinter());
    }

    @Override
    public final RegisteredService save(final RegisteredService service) {
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
    public final synchronized boolean delete(final RegisteredService service) {
        serviceMap.remove(service.getId());
        final boolean result = makeFile(service).delete();
        load();
        return result;
    }

    @Override
    public final synchronized List<RegisteredService> load() {
        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<Long, RegisteredService>();

        int errorCount = 0;

        final Collection<File> c = FileUtils.listFiles(this.serviceRegistryDirectory, new String[]{FILE_EXTENSION}, true);
        for (final File file : c) {
            BufferedInputStream in = null;
            try {
                if (file.length() > 0) {
                    in = new BufferedInputStream(new FileInputStream(file));
                    final RegisteredService service = this.objectMapper.readValue(in, RegisteredService.class);

                    temp.put(service.getId(), service);
                }
            } catch (final Exception e) {
                errorCount++;
                LOGGER.error("Error reading configuration file", e);
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
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }

    /**
     * Creates a JSON file for a registered service.
     *
     * @param  service  Registered service.
     * @return  JSON file in service registry directory.
     */
    protected File makeFile(final RegisteredService service) {
        return new File(serviceRegistryDirectory, service.getName() + "-" + service.getId() + "." + FILE_EXTENSION);
    }

    /**
     * Initialize object mapper.
     *
     * @return the object mapper
     */
    private static ObjectMapper initializeObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        mapper.addMixInAnnotations(RegisteredServiceProxyPolicy.class, RegisteredServiceProxyPolicyMixin.class);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

    private interface RegisteredServiceProxyPolicyMixin {
        /**
         * Ignore method call.
         * @return allowed or not
         **/
        @JsonIgnore
        boolean isAllowedToProxy();

        /**
         * Ignore method call.
         * @param pgtUrl proxying url
         * @return allowed or not
         **/
        @JsonIgnore
        boolean isAllowedProxyCallbackUrl(URL pgtUrl);
    }

}
