package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
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
    void verifyExecutionIsNotBlockedBySlowExecution() throws Throwable {
        val file = Files.createTempFile("slow", ".groovy").toFile();
        FileUtils.writeStringToFile(file, """
            def run(final Object... args) {
                if (args.length > 1) {
                    def release = args[0] as java.util.concurrent.CountDownLatch
                    def started = args[1] as java.util.concurrent.CountDownLatch
                    started.countDown()
                    release.await(30, java.util.concurrent.TimeUnit.SECONDS)
                }
                return 'done'
            }
            """.stripIndent(), StandardCharsets.UTF_8);

        val release = new CountDownLatch(1);
        val started = new CountDownLatch(1);
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        try (val resource = scriptFactory.fromResource(new FileSystemResource(file), false);
             val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val holder = executor.submit(() ->
                resource.execute(new Object[]{release, started}, String.class, true));
            assertTrue(started.await(30, TimeUnit.SECONDS));
            val concurrent = executor.submit(() -> resource.execute(new Object[]{0L}, String.class, true));
            assertEquals("done", concurrent.get(30, TimeUnit.SECONDS));
            assertFalse(holder.isDone());
            release.countDown();
            assertEquals("done", holder.get(30, TimeUnit.SECONDS));
        }
    }

    @Test
    void verifyStatefulScriptRunsOneAtATime() throws Throwable {
        val file = Files.createTempFile("stateful", ".groovy").toFile();
        FileUtils.writeStringToFile(file, """
            import groovy.transform.Field

            @Field int executions = 0

            def run(final Object... args) {
                executions++
                if (args.length > 1) {
                    def release = args[0] as java.util.concurrent.CountDownLatch
                    def started = args[1] as java.util.concurrent.CountDownLatch
                    started.countDown()
                    release.await(30, java.util.concurrent.TimeUnit.SECONDS)
                }
                return 'done'
            }
            """.stripIndent(), StandardCharsets.UTF_8);

        val release = new CountDownLatch(1);
        val started = new CountDownLatch(1);
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        try (val resource = scriptFactory.fromResource(new FileSystemResource(file), false);
             val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val holder = executor.submit(() ->
                resource.execute(new Object[]{release, started}, String.class, true));
            assertTrue(started.await(30, TimeUnit.SECONDS));
            val queued = executor.submit(() -> resource.execute(new Object[]{0L}, String.class, true));
            assertThrows(TimeoutException.class, () -> queued.get(250, TimeUnit.MILLISECONDS));
            release.countDown();
            assertEquals("done", holder.get(30, TimeUnit.SECONDS));
            assertEquals("done", queued.get(30, TimeUnit.SECONDS));
        }
    }

    @Test
    void verifyStatefulScriptKeepsItsState() throws Throwable {
        val file = Files.createTempFile("counter", ".groovy").toFile();
        FileUtils.writeStringToFile(file, """
            import groovy.transform.Field

            @Field int count = 0

            def run(final Object... args) {
                count++
                return count
            }
            """.stripIndent(), StandardCharsets.UTF_8);

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        try (val resource = scriptFactory.fromResource(new FileSystemResource(file), false);
             val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val results = IntStream.range(0, 64)
                .mapToObj(_ -> executor.submit(() -> resource.execute(new Object[]{0}, Integer.class, true)))
                .toList();
            val observed = new HashSet<Integer>();
            for (val result : results) {
                observed.add(result.get(30, TimeUnit.SECONDS));
            }
            assertEquals(IntStream.rangeClosed(1, 64).boxed().collect(Collectors.toSet()), observed);
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
