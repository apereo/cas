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

package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes the CAS logging framework by calling
 * the logger initializer and sets the location of the
 * log configuration file.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component
public final class CasLoggerContextInitializer implements ServletContextAware {
    private static AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(CasLoggerContextInitializer.class);

    private ServletContext context;

    private ServletContextListener loggerContext;

    private final String loggerContextPackageName;

    private final Resource logConfigurationFile;

    private final String logConfigurationField;

    /**
     * Instantiates a new Cas logger context initializer.
     */
    protected CasLoggerContextInitializer() {
        this.loggerContext = null;
        this.loggerContextPackageName = null;
        this.logConfigurationField = null;
        this.logConfigurationFile = null;
    }

    /**
     * Instantiates a new Cas logger context initializer.
     *
     * @param loggerContextPackageName the logger context package name
     * @param logConfigurationFile the log configuration file
     * @param logConfigurationField the log configuration field
     */
    public CasLoggerContextInitializer(@NotNull final String loggerContextPackageName,
                                       @NotNull final Resource logConfigurationFile,
                                       @NotNull final String logConfigurationField) {
        this.loggerContextPackageName = loggerContextPackageName;
        this.logConfigurationField = logConfigurationField;
        this.logConfigurationFile = logConfigurationFile;
    }

    /**
     * Initialize the logger by decorating the context
     * with settings for the log file and context.
     * Calls the initializer of the logging framework
     * to start the logger.
     */
    private void initialize() {
        try {
            if (!INITIALIZED.get() && this.loggerContext != null) {
                final ServletContextEvent event = new ServletContextEvent(this.context);
                this.loggerContext.contextInitialized(event);
                LOGGER.debug("Initialized logging context via [{}]. Logs will be written to [{}]",
                        this.loggerContext.getClass().getSimpleName(),
                        this.logConfigurationFile);
                INITIALIZED.set(true);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Destroys all logging hooks and shuts down
     * the logger.
     */
    @PreDestroy
    public void destroy() {
        try {
            if (INITIALIZED.get() && this.loggerContext != null) {
                final ServletContextEvent event = new ServletContextEvent(this.context);
                LOGGER.debug("Destroying logging context and shutting it down");
                this.loggerContext.contextDestroyed(event);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prepares the logger context. Locates the context and
     * sets the configuration file.
     * @return the logger context
     */
    private ServletContextListener prepareAndgetContextListener() {
        try {
            if (StringUtils.isNotBlank(this.loggerContextPackageName)) {
                final Collection<URL> set = ClasspathHelper.forPackage(this.loggerContextPackageName);
                final Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(set).setScanners(new SubTypesScanner()));
                final Set<Class<? extends ServletContextListener>> subTypesOf = reflections.getSubTypesOf(ServletContextListener.class);
                final ServletContextListener loggingContext = subTypesOf.iterator().next().newInstance();
                this.context.setInitParameter(this.logConfigurationField, this.logConfigurationFile.getURI().toString());
                return loggingContext;
            }
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Prepared the logger context with the
     * received servlet web context. Because the context
     * may be initialized twice, there are safety checks
     * added to ensure we don't create duplicate log
     * environments.</p>
     * @param servletContext
     */
    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.context = servletContext;
        this.loggerContext = prepareAndgetContextListener();
        initialize();
    }
}
