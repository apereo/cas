package org.apereo.cas.util.io;

import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import jakarta.annotation.Nullable;
import java.io.File;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Control watch operations on paths and files as a service.
 *
 * @author David Rodriguez
 * @since 5.2.0
 */
@Slf4j
public class PathWatcherService implements WatcherService, Runnable, DisposableBean {
    private static final String PROPERTY_DISABLE_WATCHER = PathWatcherService.class.getName();

    private static final WatchEvent.Kind[] KINDS = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

    @Nullable
    private WatchService watchService;

    private final Consumer<File> onCreate;

    private final Consumer<File> onModify;

    private final Consumer<File> onDelete;

    @Nullable
    private Thread thread;

    public PathWatcherService(final File watchablePath, final Consumer<File> onModify) {
        this(watchablePath.toPath(),
            __ -> {
            }, onModify,
            __ -> {
            });
    }

    public PathWatcherService(final Path watchablePath, final Consumer<File> onCreate,
                              final Consumer<File> onModify, final Consumer<File> onDelete) {
        LOGGER.info("Watching directory path at [{}]", watchablePath);
        this.onCreate = onCreate;
        this.onModify = onModify;
        this.onDelete = onDelete;
        if (shouldEnableWatchService()) {
            initializeWatchService(watchablePath);
        }
    }

    @Override
    public void run() {
        if (shouldEnableWatchService()) {
            try {
                var key = (WatchKey) null;
                while ((key = watchService.take()) != null) {
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
    }

    @Override
    public void close() {
        LOGGER.trace("Closing service registry watcher thread");
        IOUtils.closeQuietly(watchService);
        if (this.thread != null) {
            thread.interrupt();
        }
        LOGGER.trace("Closed service registry watcher thread");
    }

    @Override
    public void start(final String name) {
        if (shouldEnableWatchService()) {
            LOGGER.trace("Starting watcher thread");
            thread = Thread.ofVirtual().name(name).start(this);
        }
    }

    @Override
    public void destroy() {
        close();
    }

    protected void handleEvent(final WatchKey key) {
        key.pollEvents().forEach(Unchecked.consumer(event -> {
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
        }));
    }

    protected boolean shouldEnableWatchService() {
        val watchServiceEnabled = StringUtils.defaultIfBlank(System.getenv(PROPERTY_DISABLE_WATCHER), System.getProperty(PROPERTY_DISABLE_WATCHER));
        return StringUtils.isBlank(watchServiceEnabled) || BooleanUtils.toBoolean(watchServiceEnabled);
    }

    protected void initializeWatchService(final Path watchablePath) {
        this.watchService = FunctionUtils.doUnchecked(() -> watchablePath.getFileSystem().newWatchService());
        LOGGER.trace("Created watcher for events of type [{}]", Arrays.stream(KINDS)
            .map(WatchEvent.Kind::name)
            .collect(Collectors.joining(",")));
        FunctionUtils.doUnchecked(__ -> watchablePath.register(Objects.requireNonNull(this.watchService), KINDS));
    }
}
