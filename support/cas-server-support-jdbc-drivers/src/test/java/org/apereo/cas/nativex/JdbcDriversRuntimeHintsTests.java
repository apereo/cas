package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcDriversRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class JdbcDriversRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        assertDoesNotThrow(() -> new JdbcDriversRuntimeHints().registerHints(hints, getClass().getClassLoader()));
    }
}
