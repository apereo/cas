package org.apereo.cas.util.io;

import java.io.Closeable;

/**
 * This is {@link WatcherService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface WatcherService extends Closeable, AutoCloseable {

    /**
     * No op watcher service.
     *
     * @return the watcher service
     */
    static WatcherService noOp() {
        return new WatcherService() {
        };
    }

    /**
     * Close.
     */
    @Override
    default void close() {
    }

    /**
     * Start the watch.
     *
     * @param name the name
     */
    default void start(String name) {
    }
}
