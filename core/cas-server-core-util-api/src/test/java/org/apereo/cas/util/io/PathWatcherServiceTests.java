package org.apereo.cas.util.io;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private static File createTemporaryFile(final String name) throws Exception {
        val filePath = new File(FileUtils.getTempDirectory(), name);
        if (filePath.exists()) {
            FileUtils.deleteQuietly(filePath);
        }
        val res = filePath.createNewFile();
        if (!res) {
            throw new IllegalStateException("Could not create file " + filePath);
        }
        return filePath;
    }

    @Test
    void verifyOperation() throws Throwable {
        val file1 = createTemporaryFile("file1.txt");
        val file2 = createTemporaryFile("file2.txt");

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

    @Test
    void verifyWatchKeyIsResetOnlyOnceHandled() throws Exception {
        val mockedKey = mock(WatchKey.class);
        val count = new AtomicInteger(0);
        doAnswer(invocation -> {
            count.incrementAndGet();
            return List.of();
        })
            .when(mockedKey)
            .pollEvents();

        val mockWatchService = mock(WatchService.class);
        when(mockWatchService.take()).thenAnswer(invocation -> count.get() == 0 ? mockedKey : null);

        try (val service = new PathWatcherService(mockWatchService, _ -> {
        })) {
            service.run();
            val inOrder = inOrder(mockedKey);
            inOrder.verify(mockedKey, times(1)).pollEvents();
            inOrder.verify(mockedKey, times(1)).reset();
        }
    }
}
