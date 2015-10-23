/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.slf4j.impl;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.slf4j.helpers.Util;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link org.slf4j.ILoggerFactory} that is looked up via the
 * {@link org.slf4j.impl.StaticLoggerBinder} of CAS itself. It is responsible for
 * creating {@link org.slf4j.Logger} instances and passing them back to the slf4j engine.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class CasLoggerFactory implements ILoggerFactory {

    private static final String PACKAGE_TO_SCAN = "org.slf4j.impl";

    private final Map<String, CasDelegatingLogger> loggerMap;

    private final Class<? extends ILoggerFactory> realLoggerFactoryClass;
    /**
     * Instantiates a new Cas logger factory.
     * Configures the reflection scanning engine to be prepared to scan <code>org.slf4j.impl</code>
     * in order to find other available factories.
     */
    public CasLoggerFactory() {
        this.loggerMap = new ConcurrentHashMap<>();
        final Collection<URL> set = ClasspathHelper.forPackage(PACKAGE_TO_SCAN);
        final Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(set).setScanners(new SubTypesScanner()));

        final Set<Class<? extends ILoggerFactory>> subTypesOf = reflections.getSubTypesOf(ILoggerFactory.class);
        subTypesOf.remove(this.getClass());

        if (subTypesOf.size() > 1) {
            Util.report("Multiple ILoggerFactory bindings are found on the classpath:");
            for (final Class<? extends ILoggerFactory> c : subTypesOf) {
                Util.report("* " + c.getCanonicalName());
            }
        }

        if (subTypesOf.isEmpty()) {
            final RuntimeException e = new RuntimeException("No ILoggerFactory could be found on the classpath."
                    + " CAS cannot determine the logging framework."
                    + " Examine the project dependencies and ensure that there is one and only one logging framework available.");

            Util.report(e.getMessage(), e);
            throw e;
        }
        this.realLoggerFactoryClass = subTypesOf.iterator().next();
        Util.report("ILoggerFactory to be used for logging is: " + this.realLoggerFactoryClass.getName());
    }

    /**
     * {@inheritDoc}
     * <p>Attempts to find the <strong>real</strong> <code>Logger</code> instance that
     * is doing the heavy lifting and routes the request to an instance of
     * {@link CasDelegatingLogger}. The instance is cached by the logger name.</p>
     */
    @Override
    public Logger getLogger(final String name) {
        if (StringUtils.isBlank(name)) {
            return NOPLogger.NOP_LOGGER;
        }
        synchronized (loggerMap) {
            if (!loggerMap.containsKey(name)) {
                final Logger logger = getRealLoggerInstance(name);
                loggerMap.put(name, new CasDelegatingLogger(logger));
            }
            return loggerMap.get(name);
        }
    }

    /**
     * Find the actual <code>Logger</code> instance that is available on the classpath.
     * This is usually the logger adapter that is provided by the real logging framework,
     * such as log4j, etc. The method will scan the runtime to find logger factories that
     * are of type {@link org.slf4j.ILoggerFactory}. It will remove itself from this list
     * first and then attempts to locate the next best factory from which real logger instances
     * can be created.
     * @param name requested logger name
     * @return the logger instance created by the logger factory available on the classpath during runtime, or null.
     */
    private Logger getRealLoggerInstance(final String name) {
        try {
            final ILoggerFactory factInstance = this.realLoggerFactoryClass.newInstance();
            return factInstance.getLogger(name);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
