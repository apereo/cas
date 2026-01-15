package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintExtractor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationTrustedHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class MultifactorAuthenticationTrustedHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new MultifactorAuthenticationTrustedHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(DeviceFingerprintExtractor.class).test(hints));
    }
}
