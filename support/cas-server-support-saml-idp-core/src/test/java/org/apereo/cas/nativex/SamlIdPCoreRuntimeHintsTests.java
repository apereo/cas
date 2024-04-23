package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link SamlIdPCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
public class SamlIdPCoreRuntimeHintsTests {

    @Test
    void verifyHints() throws Throwable {
        val hints = new RuntimeHints();
        new SamlIdPCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
    }
}
