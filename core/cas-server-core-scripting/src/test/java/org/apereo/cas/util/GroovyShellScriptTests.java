package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyShellScriptTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
@Slf4j
class GroovyShellScriptTests {

    @Nested
    @SetSystemProperty(key = ExecutableCompiledScriptFactory.SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC, value = "true")
    class StaticCompilationTests {
        @Test
        void verifyOperation() {
            val script =
                """
                    def logger = (Logger) binding.getVariable('logger')
                    def attributes = (Map) binding.getVariable('attributes')
                    logger.info('Attributes: {}', attributes)
                    
                    if ((attributes.get('entitlement') as List).contains('admin')) {
                        return [(attributes['uid'] as List).get(0).toString().toUpperCase()]
                    } else {
                        return attributes['identifier'] as List
                    }
                    """;
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            val shellScript = scriptFactory.fromScript(script.stripIndent());

            val attributes1 = new HashMap<String, Object>();
            attributes1.put("entitlement", List.of("admin"));
            attributes1.put("uid", List.of("casadmin"));
            attributes1.put("identifier", List.of("1984"));
            new RunnableScript(attributes1, shellScript, "CASADMIN").run();
        }
    }

    @Nested
    @ClearSystemProperty(key = ExecutableCompiledScriptFactory.SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC)
    class ConcurrentTests {
        @Test
        void verifyOperation() {
            val script =
                """
                    if (attributes['entitlement'].contains('admin')) {
                        return [attributes['uid'].get(0).toUpperCase()]
                    } else {
                        return attributes['identifier']
                    }
                    """;

            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            val shellScript = scriptFactory.fromScript(script.stripIndent());

            val attributes1 = new HashMap<String, Object>();
            attributes1.put("entitlement", List.of("admin"));
            attributes1.put("uid", List.of("casadmin"));
            attributes1.put("identifier", List.of("1984"));

            val attributes2 = new HashMap<String, Object>();
            attributes2.put("entitlement", List.of("user"));
            attributes2.put("uid", List.of("casuser"));
            attributes2.put("identifier", List.of("123456"));

            val testHasFailed = new AtomicBoolean();
            val threads = new ArrayList<Thread>();
            for (var i = 1; i <= 50; i++) {
                val runnable = i % 2 == 0
                    ? new RunnableScript(attributes1, shellScript, "CASADMIN")
                    : new RunnableScript(attributes2, shellScript, "123456");
                val thread = new Thread(runnable);
                thread.setName("Thread-" + i);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    LoggingUtils.error(LOGGER, e);
                    testHasFailed.set(true);
                });
                threads.add(thread);
                thread.start();
            }
            for (val thread : threads) {
                try {
                    thread.join();
                } catch (final Throwable e) {
                    fail(e);
                }
            }
            if (testHasFailed.get()) {
                fail("Test failed");
            }
        }
    }

    @Nested
    @ClearSystemProperty(key = ExecutableCompiledScriptFactory.SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC)
    class BindingTests {
        private static final String SCRIPT = "return binding.variables['principal'] ?: 'none'";

        @Test
        void verifyBindingIsConsumedByOneExecutionOnly() {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val shellScript = scriptFactory.fromScript(SCRIPT)) {
                shellScript.setBinding(CollectionUtils.wrap("principal", "alice"));
                assertEquals("alice", shellScript.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true));
                assertEquals("none", shellScript.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true));
            }
        }

        @Test
        void verifyRepeatedExecutionWithoutABinding() {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val shellScript = scriptFactory.fromScript(SCRIPT)) {
                for (var i = 0; i < 3; i++) {
                    assertEquals("none", shellScript.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true),
                        "A cached script executed without a binding must keep producing a result");
                }
            }
        }

        @Test
        void verifyAbandonedBindingIsNotVisibleToAnotherScript() {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val abandoned = scriptFactory.fromScript(SCRIPT);
                 val unrelated = scriptFactory.fromScript(SCRIPT)) {
                abandoned.setBinding(CollectionUtils.wrap("principal", "alice"));
                assertEquals("none", unrelated.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true));
                assertEquals("none", abandoned.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true));
            }
        }

        @Test
        void verifyBindingIsNotSharedAcrossConcurrentThreads() throws Exception {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val shellScript = scriptFactory.fromScript(SCRIPT)) {
                val count = 64;
                val barrier = new CyclicBarrier(count);
                val results = new ArrayList<Future<Pair<String, String>>>();
                try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    for (var i = 0; i < count; i++) {
                        val expected = "principal-" + i;
                        results.add(executor.submit(() -> {
                            barrier.await(60, TimeUnit.SECONDS);
                            shellScript.setBinding(CollectionUtils.wrap("principal", expected));
                            return Pair.of(expected, shellScript.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true));
                        }));
                    }
                    val observed = new HashSet<String>();
                    for (val result : results) {
                        val outcome = result.get(60, TimeUnit.SECONDS);
                        assertEquals(outcome.getLeft(), outcome.getRight(), "Script observed a binding that belongs to another thread");
                        observed.add(outcome.getRight());
                    }
                    assertEquals(count, observed.size());
                }
            }
        }

        @Test
        void verifyBindingDoesNotCarryOverBetweenAttemptsOnPooledThreads() throws Exception {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val shellScript = scriptFactory.fromScript(SCRIPT)) {
                val results = new ArrayList<Future<Pair<String, String>>>();
                try (val executor = Executors.newFixedThreadPool(4)) {
                    for (var i = 0; i < 200; i++) {
                        val expected = "principal-" + i;
                        if (i % 5 == 0) {
                            results.add(executor.submit(() -> {
                                shellScript.setBinding(CollectionUtils.wrap("principal", "aborted-" + expected));
                                return Pair.of("aborted", "aborted");
                            }));
                        }
                        results.add(executor.submit(() -> {
                            shellScript.setBinding(CollectionUtils.wrap("principal", expected));
                            return Pair.of(expected, shellScript.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class, true));
                        }));
                    }
                    for (val result : results) {
                        val outcome = result.get(60, TimeUnit.SECONDS);
                        assertEquals(outcome.getLeft(), outcome.getRight(), "Script observed a binding left behind by an earlier attempt");
                    }
                }
            }
        }
    }

    @Nested
    @ClearSystemProperty(key = ExecutableCompiledScriptFactory.SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC)
    class DefaultTests {
        @Test
        void verifyExec() {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val shell = scriptFactory.fromScript("println 'test'")) {
                assertNotNull(shell.getResource());
                assertNull(shell.getCompiledScript());
                assertDoesNotThrow(() -> shell.execute(ArrayUtils.EMPTY_OBJECT_ARRAY));
                assertNotNull(shell.getCompiledScript());
                assertNotNull(shell.toString());
            }
        }

        @Test
        void verifyUnknownBadScript() {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val shell = scriptFactory.fromScript("###$$@@@!!!***&&&")) {
                assertDoesNotThrow(() -> {
                    shell.execute(ArrayUtils.EMPTY_OBJECT_ARRAY);
                    shell.execute("run", Void.class, ArrayUtils.EMPTY_OBJECT_ARRAY);
                });
            }
        }
    }

    @RequiredArgsConstructor
    private static final class RunnableScript implements Runnable {
        private final Map<String, Object> attributes;
        private final ExecutableCompiledScript shellScript;
        private final Object expectedAttribute;

        @Override
        public void run() {
            try {
                shellScript.setBinding(CollectionUtils.wrap("attributes", attributes));
                val returnValue = shellScript.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, List.class);
                assertEquals(1, returnValue.size());
                assertEquals(expectedAttribute, returnValue.getFirst());
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
