package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InfluxDbRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Native")
class InfluxDbRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new InfluxDbRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onFieldAccess(Class.forName("java.nio.Bits").getDeclaredField("UNALIGNED")).test(hints));
    }
}
