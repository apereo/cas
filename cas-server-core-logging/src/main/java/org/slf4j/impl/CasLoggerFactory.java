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
import java.util.HashSet;
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

    private static final String ENVIRONMENT_VAR_LOGGER_FACTORY = "loggerFactory";

    private static final String PACKAGE_TO_SCAN = "org.slf4j.impl";

    private final Map<String, CasDelegatingLogger> loggerMap;

    private Class<? extends ILoggerFactory> realLoggerFactoryClass;
    /**
     * Instantiates a new Cas logger factory.
     * Configures the reflection scanning engine to be prepared to scan {@code org.slf4j.impl}
     * in order to find other available factories.
     */
    public CasLoggerFactory() {
        this.loggerMap = new ConcurrentHashMap<>();

        Set<Class<? extends ILoggerFactory>> loggerFactories = lookUpConfiguredLoggerFactory();
        if (loggerFactories.isEmpty()) {
            loggerFactories = scanContextForLoggerFactories();
        }

        if (loggerFactories.size() > 1) {
            Util.report("Multiple ILoggerFactory bindings are found on the classpath:");
            for (final Class<? extends ILoggerFactory> c : loggerFactories) {
                Util.report("* " + c.getCanonicalName());
            }
            Util.report("This generally indicates a configuration problem which is a result of dependency conflicts.");
            Util.report("If you wish to use a different logging framework, specify the ILoggerFactory binding via -D"
                    + ENVIRONMENT_VAR_LOGGER_FACTORY + "=<binding-class> to the runtime environment");
        }

        if (loggerFactories.isEmpty()) {
            final RuntimeException e = new RuntimeException("No ILoggerFactory could be found on the classpath."
                    + " CAS cannot determine the logging framework."
                    + " Examine the project dependencies and ensure that there is one and only one logging framework available.");

            Util.report(e.getMessage(), e);
            throw e;
        }
        this.realLoggerFactoryClass = null;
        for (final Class<? extends ILoggerFactory> factory : loggerFactories) {
            Util.report("Attempting to locate ILoggerFactory instance from: " + factory.getName());
            if (getLoggerFactoryBeInstantiated(factory) != null) {
                this.realLoggerFactoryClass = factory;
                Util.report("ILoggerFactory to be used for logging is: " + this.realLoggerFactoryClass.getName());
                break;
            } else {
                Util.report("ILoggerFactory [" + factory.getName() + "] could not be used. Trying the next ILoggerFactory...");
            }
        }

        if (this.realLoggerFactoryClass == null) {
            throw new RuntimeException("No ILoggerFactory is available to use. Log configuration is incorrect, "
                            + "or multiple logging frameworks are at conflict with one another on the classpath.");
        }
    }

    private Set<Class<? extends ILoggerFactory>> scanContextForLoggerFactories() {
        final Set<Class<? extends ILoggerFactory>> loggerFactories;
        final Collection<URL> set = ClasspathHelper.forPackage(PACKAGE_TO_SCAN);
        final Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(set).setScanners(new SubTypesScanner()));

        loggerFactories = reflections.getSubTypesOf(ILoggerFactory.class);
        loggerFactories.remove(this.getClass());
        return loggerFactories;
    }

    /**
     * {@inheritDoc}
     * <p>Attempts to find the <strong>real</strong> {@code Logger} instance that
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

    private Set<Class<? extends ILoggerFactory>> lookUpConfiguredLoggerFactory() {
        final Set<Class<? extends ILoggerFactory>> loggerFactories = new HashSet<>();

        final String configuredLoggerFactory = System.getProperty(ENVIRONMENT_VAR_LOGGER_FACTORY);
        if (StringUtils.isNotBlank(configuredLoggerFactory)) {
            Util.report("Instructed logger factory to use is " + configuredLoggerFactory);
            try {
                final Class clazz = Class.forName(configuredLoggerFactory);
                loggerFactories.add(clazz);
            } catch (final Exception e) {
                Util.report("Could not locate the provided logger factory: " + configuredLoggerFactory + ". Error: " + e.getMessage());
            }
        }

        return loggerFactories;
    }
    /**
     * Find the actual {@code Logger} instance that is available on the classpath.
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
            final ILoggerFactory factInstance = getLoggerFactoryBeInstantiated(this.realLoggerFactoryClass);
            if (factInstance == null) {
                throw new RuntimeException("ILoggerFactory cannot be created from "
                        + this.realLoggerFactoryClass.getName());
            }
            return factInstance.getLogger(name);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static ILoggerFactory getLoggerFactoryBeInstantiated(final Class<? extends ILoggerFactory> loggerFactory) {
        try {
            return loggerFactory.newInstance();
        } catch (final Exception e) {
            return null;
        }
    }
}
