package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.web.support.ThrottledSubmission;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ThrottlingRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class ThrottlingRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new ThrottlingRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(hints.reflection().getTypeHint(ThrottledSubmission.class).hasJavaSerialization());
    }
}
