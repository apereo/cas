package org.apereo.cas.util.io;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author David Rodriguez
 * @since 5.2.0
 */
@Slf4j
public class PathWatcherService implements Runnable, Closeable {

    private static final WatchEvent.Kind[] KINDS = new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

    private Thread thread;

    private final WatchService watcher;
    private final Consumer<File> onCreate;
    private final Consumer<File> onModify;
    private final Consumer<File> onDelete;

    public PathWatcherService(final File watchablePath, final Consumer<File> onModify) {
        this(watchablePath.toPath(),
            file -> {
            }, onModify,
            file -> {
            });
    }

    /**
     * Instantiates a new Json service registry config watcher.
     *
     * @param watchablePath path that will be watched
     * @param onCreate      action triggered when a new file is created
     * @param onModify      action triggered when a file is modified
     * @param onDelete      action triggered when a file is deleted
     */
    @SneakyThrows
    public PathWatcherService(final Path watchablePath, final Consumer<File> onCreate,
                              final Consumer<File> onModify, final Consumer<File> onDelete) {
        this.onCreate = onCreate;
        this.onModify = onModify;
        this.onDelete = onDelete;
        this.watcher = watchablePath.getFileSystem().newWatchService();
        LOGGER.debug("Created service registry watcher for events of type [{}]", (Object[]) KINDS);
        watchablePath.register(this.watcher, KINDS);
    }

    @Override
    public void run() {
        try {
            WatchKey key;
            while ((key = watcher.take()) != null) {
                handleEvent(key);
                /*
                    Reset the key -- this step is critical to receive
                    further watch events. If the key is no longer valid, the directory
                    is inaccessible so exit the loop.
                 */
                final boolean valid = key != null && key.reset();
                if (!valid) {
                    LOGGER.info("Directory key is no longer valid. Quitting watcher service");
                }
            }
        } catch (final InterruptedException e) {
            return;
        }
    }

    /**
     * Handle event.
     *
     * @param key the key
     */
    private void handleEvent(final WatchKey key) {
        try {
            key.pollEvents().stream().forEach(event -> {
                final String eventName = event.kind().name();

                // The filename is the context of the event.
                final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                final Path filename = ev.context();

                final Path parent = (Path) key.watchable();
                final Path fullPath = parent.resolve(filename);
                final File file = fullPath.toFile();

                LOGGER.trace("Detected event [{}] on file [{}]", eventName, file);
                if (eventName.equals(ENTRY_CREATE.name()) && file.exists()) {
                    onCreate.accept(file);
                } else if (eventName.equals(ENTRY_DELETE.name())) {
                    onDelete.accept(file);
                } else if (eventName.equals(ENTRY_MODIFY.name()) && file.exists()) {
                    onModify.accept(file);
                }
            });
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.watcher);
        if (this.thread != null) {
            thread.interrupt();
        }
    }


    /**
     * Start thread.
     *
     * @param name the name
     */
    @SneakyThrows
    public void start(final String name) {
        this.thread = new Thread(this);
        this.thread.setName(name);
        thread.start();
    }
}
