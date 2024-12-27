package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JsonServiceRegistryRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class JsonServiceRegistryRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new JsonServiceRegistryRuntimeHints().registerHints(hints, getClass().getClassLoader());
    }
}
