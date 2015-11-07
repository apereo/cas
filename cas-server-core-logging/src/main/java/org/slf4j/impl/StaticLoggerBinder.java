package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * The static binder for slf4j logging, which allows CAS
 * to select its own {@link org.slf4j.ILoggerFactory} instance at runtime.
 * Note that this class MUST reside in the {@code org.slf4j.impl}
 * package so it can be loaded by the runtime dynamic lookup.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class StaticLoggerBinder implements LoggerFactoryBinder {

    /**
     * The unique instance of this class.
     */
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    /**
     * The {@link ILoggerFactory} instance returned by the
     * {@link #getLoggerFactory} method should always be the same
     * object.
     */
    private final ILoggerFactory loggerFactory;

    /**
     * Instantiates a new Static logger binder.
     */
    private StaticLoggerBinder() {
        this.loggerFactory = new CasLoggerFactory();
    }

    /**
     * Return the singleton of this class.
     *
     * @return the StaticLoggerBinder singleton
     */
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return this.loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return CasLoggerFactory.class.getName();
    }
}
