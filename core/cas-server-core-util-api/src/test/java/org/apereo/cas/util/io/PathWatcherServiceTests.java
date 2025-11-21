package org.apereo.cas.util.io;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PathWatcherServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@Tag("FileSystem")
class PathWatcherServiceTests {
    private PathWatcherService watcher1;

    private FileWatcherService watcher2;

    private static File createFile(final String name) throws Exception {
        val path1 = new File(FileUtils.getTempDirectory(), name);
        val res = path1.createNewFile();
        if (res) {
            LOGGER.debug("Created JSON resource @ [{}]", path1);
        }
        return path1;
    }

    @Test
    void verifyOperation() throws Throwable {
        val file1 = createFile("file1.txt");
        val file2 = createFile("file2.txt");

        val watch1 = new AtomicBoolean();
        watcher1 = new PathWatcherService(file1.getParentFile(), file -> {
            watch1.set(true);
            LOGGER.debug("[{}] is modified", file1.getName());
        });

        val watch2 = new AtomicBoolean();
        watcher2 = new FileWatcherService(file2, file -> {
            watch2.set(true);
            LOGGER.debug("[{}] is modified", file2.getName());
        });

        val changeThread = new Thread(Unchecked.runnable(() -> {
            FileUtils.writeStringToFile(file1, "1", StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(file2, "2", StandardCharsets.UTF_8);
            Thread.sleep(10_000);
        }));

        watcher2.start(file1.getName());
        watcher1.start(file2.getName());

        changeThread.start();
        changeThread.join();

        assertTrue(watch1.get());
        assertTrue(watch2.get());

        watcher1.destroy();
    }

    static class MockWatchService implements WatchService {
        private WatchKey key;
        private AtomicInteger count;

        public MockWatchService(WatchKey key) {
            this.key = key;
            this.count = new AtomicInteger(0);

            doAnswer(invocation -> {
                this.count.incrementAndGet();
                return List.of();
            })
                .when(key)
                .pollEvents();
        }

        @Override
        public WatchKey take() {
            return (count.get() == 0) ? this.key : null;
        }

        @Override
        public WatchKey poll() {
            return null;
        }

        @Override
        public WatchKey poll(long timeout, TimeUnit unit) {
            return null;
        }

        @Override
        public void close() {
            // no-op
        }
    }

    static class MockedPathWatcherService extends PathWatcherService {
        public MockedPathWatcherService(WatchKey key) throws Exception {
            super(createFile("test.txt").toPath(), x -> {}, x -> {}, x -> {});

            this.watchService = new MockWatchService(key);
        }

        @Override
        protected void initializeWatchService(final Path watchablePath) {
            // no-op
        }
    }

    @Test
    void verifyWatchKeyIsResetOnlyOnceHandled() throws Exception {
        val mockedKey = mock(WatchKey.class);
        val service = new MockedPathWatcherService(mockedKey);

        service.run();

        InOrder inOrder = inOrder(mockedKey);

        inOrder.verify(mockedKey, times(1)).pollEvents();
        inOrder.verify(mockedKey, times(1)).reset();
    }
}
