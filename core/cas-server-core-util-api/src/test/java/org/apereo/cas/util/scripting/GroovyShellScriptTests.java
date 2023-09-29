package org.apereo.cas.util.scripting;

import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

            val shellScript = new GroovyShellScript(script.stripIndent());

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
                    ? new ScriptedAttribute(attributes1, shellScript, "CASADMIN")
                    : new ScriptedAttribute(attributes2, shellScript, "123456");
                val thread = new Thread(runnable);
                thread.setName("Thread-" + i);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    LOGGER.error(e.getMessage(), e);
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

        @RequiredArgsConstructor
        private static final class ScriptedAttribute implements Runnable {
            private final Map<String, Object> attributes;
            private final GroovyShellScript shellScript;
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

    @Nested
    class DefaultTests {
        @Test
        void verifyExec() {
            try (val shell = new GroovyShellScript("println 'test'")) {
                assertNotNull(shell.getGroovyScript());
                assertNotNull(shell.getScript());

                assertDoesNotThrow(() -> shell.execute(ArrayUtils.EMPTY_OBJECT_ARRAY));
                assertNotNull(shell.toString());
            }
        }

        @Test
        void verifyUnknownBadScript() {
            try (val shell = new GroovyShellScript("###$$@@@!!!***&&&")) {
                assertDoesNotThrow(() -> {
                    shell.execute(ArrayUtils.EMPTY_OBJECT_ARRAY);
                    shell.execute("run", Void.class, ArrayUtils.EMPTY_OBJECT_ARRAY);
                });
            }
        }
    }
}
