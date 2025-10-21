package org.apereo.cas.util;

import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
