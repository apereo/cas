package org.apereo.cas.util;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public class PathWatcher implements Runnable, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathWatcher.class);
    private static final WatchEvent.Kind[] KINDS = new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = this.lock.readLock();

    private final WatchService watcher;
    private final Consumer<File> onCreate;
    private final Consumer<File> onModify;
    private final Consumer<File> onDelete;
    private long interval;

    /**
     * Instantiates a new Json service registry config watcher.
     *
     * @param watchablePath path that will be watched
     * @param onCreate action triggered when a new file is created
     * @param onModify action triggered when a file is modified
     * @param onDelete action triggered when a file is deleted
     */
    public PathWatcher(final Path watchablePath, final Consumer<File> onCreate, final Consumer<File> onModify, final Consumer<File> onDelete) {
        this(watchablePath, onCreate, onModify, onDelete, 0);
    }

    /**
     * Instantiates a new Json service registry config watcher.
     *
     * @param watchablePath path that will be watched
     * @param onCreate action triggered when a new file is created
     * @param onModify action triggered when a file is modified
     * @param onDelete action triggered when a file is deleted
     * @param interval milliseconds interval to limit monitoring
     */
    public PathWatcher(final Path watchablePath, final Consumer<File> onCreate, final Consumer<File> onModify, final Consumer<File> onDelete,
                       final long interval) {
        this.onCreate = onCreate;
        this.onModify = onModify;
        this.onDelete = onDelete;
        this.interval = interval;
        try {
            this.watcher = watchablePath.getFileSystem().newWatchService();
            LOGGER.debug("Created service registry watcher for events of type [{}]", (Object[]) KINDS);
            watchablePath.register(this.watcher, KINDS);
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void run() {
        if (this.running.compareAndSet(false, true)) {
            long lastModified = System.currentTimeMillis();
            while (this.running.get()) {
                // wait for key to be signaled
                WatchKey key = null;
                try {
                    key = this.watcher.take();
                    if (System.currentTimeMillis() - lastModified >= interval) {
                        handleEvent(key);
                        lastModified = System.currentTimeMillis();
                    }
                } catch (final InterruptedException e) {
                    return;
                } finally {
                    /*
                        Reset the key -- this step is critical to receive
                        further watch events. If the key is no longer valid, the directory
                        is inaccessible so exit the loop.
                     */
                    final boolean valid = key != null && key.reset();
                    if (!valid) {
                        LOGGER.warn("Directory key is no longer valid. Quitting watcher service");
                    }
                }
            }
        }
    }

    /**
     * Handle event.
     *
     * @param key the key
     */
    private void handleEvent(final WatchKey key) {
        this.readLock.lock();
        try {
            key.pollEvents().stream().filter(event -> event.count() <= 1).forEach(event -> {
                final String eventName = event.kind().name();

                //The filename is the context of the event.
                final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                final Path filename = ev.context();

                final Path parent = (Path) key.watchable();
                final Path fullPath = parent.resolve(filename);
                final File file = fullPath.toFile();

                LOGGER.trace("Detected event [{}] on file [{}]. Loading change...", eventName, file);
                if (eventName.equals(ENTRY_CREATE.name()) && file.exists()) {
                    onCreate.accept(file);
                } else if (eventName.equals(ENTRY_DELETE.name())) {
                    onDelete.accept(file);
                } else if (eventName.equals(ENTRY_MODIFY.name()) && file.exists()) {
                    onModify.accept(file);
                }
            });
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.watcher);
    }
}
