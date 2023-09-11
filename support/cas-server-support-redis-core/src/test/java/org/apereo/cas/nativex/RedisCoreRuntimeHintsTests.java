package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link RedisCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class RedisCoreRuntimeHintsTests {
    @Test
    void verifyHints() throws Throwable {
        val hints = new RuntimeHints();
        new RedisCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
    }
}
