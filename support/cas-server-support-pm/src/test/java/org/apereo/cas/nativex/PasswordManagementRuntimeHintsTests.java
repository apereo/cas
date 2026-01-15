package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link PasswordManagementRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Native")
class PasswordManagementRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new PasswordManagementRuntimeHints().registerHints(hints, getClass().getClassLoader());
    }
}
