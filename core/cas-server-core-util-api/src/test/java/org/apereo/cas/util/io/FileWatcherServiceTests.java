package org.apereo.cas.util.io;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.awaitility.Awaitility.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FileWatcherServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@Tag("FileSystem")
class FileWatcherServiceTests {
    private FileWatcherService watcher1;

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
        watcher1 = new FileWatcherService(file1, file -> {
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
        }));

        watcher1.start(file1.getName());
        watcher2.start(file2.getName());

        changeThread.start();
        changeThread.join();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertTrue(watch1.get()));
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertTrue(watch2.get()));
    }
}
