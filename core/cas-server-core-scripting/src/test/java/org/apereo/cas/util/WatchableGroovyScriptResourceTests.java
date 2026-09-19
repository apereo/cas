package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WatchableGroovyScriptResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
class WatchableGroovyScriptResourceTests {

    @Test
    void verifyQueuedExecutionIsNotDroppedWhenScriptIsSlow() throws Throwable {
        val file = Files.createTempFile("slow", ".groovy").toFile();
        FileUtils.writeStringToFile(file, """
            def run(final Object... args) {
                if (args.length > 1) {
                    new File(args[1] as String).text = 'running'
                }
                Thread.sleep(args[0] as long)
                return 'done'
            }
            """.stripIndent(), StandardCharsets.UTF_8);

        val marker = Files.createTempFile("running", ".txt").toFile();
        assertTrue(marker.delete());

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        try (val resource = scriptFactory.fromResource(new FileSystemResource(file), false);
             val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val holder = executor.submit(() ->
                resource.execute(new Object[]{2_000L, marker.getAbsolutePath()}, String.class, true));
            await().atMost(Duration.ofSeconds(30)).until(marker::exists);
            val queued = executor.submit(() -> resource.execute(new Object[]{0L}, String.class, true));
            assertEquals("done", holder.get(30, TimeUnit.SECONDS));
            assertEquals("done", queued.get(30, TimeUnit.SECONDS));
        }
    }

    @Test
    void verifyOperation() throws Throwable {
        val file = Files.createTempFile("file", ".groovy").toFile();
        FileUtils.writeStringToFile(file, "println 'hello'", StandardCharsets.UTF_8);

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        try (val resource = scriptFactory.fromResource(new FileSystemResource(file))) {
            assertDoesNotThrow(() -> resource.execute(ArrayUtils.EMPTY_OBJECT_ARRAY));
        }
        Files.setLastModifiedTime(file.toPath(), FileTime.from(Instant.now()));
    }
}
