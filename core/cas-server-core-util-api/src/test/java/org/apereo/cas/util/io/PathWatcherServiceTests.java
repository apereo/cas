package org.apereo.cas.util.io;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.awaitility.Awaitility.*;
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

    /**
     * Creates a file inside the given directory. The directory is one this test made for itself,
     * because the watcher below is pointed at a whole directory rather than a single file: aimed at
     * the shared temporary directory it would answer to every other test in this JVM that happens to
     * write there, and the files it expects to find would be named the same as theirs.
     *
     * @param directory the directory to create the file in
     * @param name      the name of the file
     * @return the created file
     * @throws Exception in case of failure
     */
    private static File createTemporaryFile(final File directory, final String name) throws Exception {
        val filePath = new File(directory, name);
        val res = filePath.createNewFile();
        if (!res) {
            throw new IllegalStateException("Could not create file " + filePath);
        }
        return filePath;
    }

    @Test
    void verifyOperation() throws Throwable {
        val directory = Files.createTempDirectory("path-watcher").toFile();
        val file1 = createTemporaryFile(directory, "file1.txt");
        val file2 = createTemporaryFile(directory, "file2.txt");

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

        watcher2.start(file1.getName());
        watcher1.start(file2.getName());

        FileUtils.writeStringToFile(file1, "1", StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(file2, "2", StandardCharsets.UTF_8);

        await().atMost(Duration.ofSeconds(30)).until(watch1::get);
        await().atMost(Duration.ofSeconds(30)).until(watch2::get);

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
