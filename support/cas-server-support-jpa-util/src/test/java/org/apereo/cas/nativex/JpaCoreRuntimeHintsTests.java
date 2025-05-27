package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JpaCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Native")
class JpaCoreRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new JpaCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
    }
}
