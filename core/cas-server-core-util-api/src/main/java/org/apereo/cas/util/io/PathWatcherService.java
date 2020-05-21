package org.apereo.cas.util.io;

import com.sun.nio.file.SensitivityWatchEventModifier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.DisposableBean;

import java.io.Closeable;
import java.io.File;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author David Rodriguez
 * @since 5.2.0
 */
@Slf4j
public class PathWatcherService implements WatcherService, Runnable, Closeable, DisposableBean {

    private static final WatchEvent.Kind[] KINDS = new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
    
    private final WatchService watcher;
    private final Consumer<File> onCreate;
    private final Consumer<File> onModify;
    private final Consumer<File> onDelete;
    private Thread thread;

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
        LOGGER.info("Watching directory path at [{}]", watchablePath);
        this.onCreate = onCreate;
        this.onModify = onModify;
        this.onDelete = onDelete;
        this.watcher = watchablePath.getFileSystem().newWatchService();
        LOGGER.trace("Created watcher for events of type [{}]", Arrays.stream(KINDS)
            .map(WatchEvent.Kind::name)
            .collect(Collectors.joining(",")));
        watchablePath.register(this.watcher, KINDS, SensitivityWatchEventModifier.HIGH);
    }

    @Override
    public void run() {
        try {
            var key = (WatchKey) null;
            while ((key = watcher.take()) != null) {
                handleEvent(key);
                val valid = key.reset();
                if (!valid) {
                    LOGGER.info("Directory key is no longer valid. Quitting watcher service");
                }
            }
        } catch (final InterruptedException | ClosedWatchServiceException e) {
            LOGGER.trace(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Handle event.
     *
     * @param key the key
     */
    private void handleEvent(final WatchKey key) {
        try {
            key.pollEvents().forEach(event -> {
                val eventName = event.kind().name();

                val ev = (WatchEvent<Path>) event;
                val filename = ev.context();

                val parent = (Path) key.watchable();
                val fullPath = parent.resolve(filename);
                val file = fullPath.toFile();

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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(e.getMessage(), e);
            } else {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        LOGGER.trace("Closing service registry watcher thread");
        IOUtils.closeQuietly(this.watcher);
        if (this.thread != null) {
            thread.interrupt();
        }
        LOGGER.trace("Closed service registry watcher thread");
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    @SneakyThrows
    public void start(final String name) {
        LOGGER.trace("Starting watcher thread");
        thread = new Thread(this);
        thread.setName(name);
        thread.start();
    }
}
