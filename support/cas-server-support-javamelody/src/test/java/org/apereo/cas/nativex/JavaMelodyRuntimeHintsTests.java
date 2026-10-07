package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JavaMelodyRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class JavaMelodyRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JavaMelodyRuntimeHints().registerHints(hints, getClass().getClassLoader());
        val type = Class.forName("com.sun.management.ThreadMXBean");
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(type.getDeclaredMethod("isThreadAllocatedMemorySupported")).test(hints));
    }
}
