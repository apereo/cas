package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link InfluxDbRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Native")
class InfluxDbRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new InfluxDbRuntimeHints().registerHints(hints, getClass().getClassLoader());
    }
}
