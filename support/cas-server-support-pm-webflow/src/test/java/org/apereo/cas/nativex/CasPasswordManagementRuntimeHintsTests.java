package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.web.CaptchaActivationStrategy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasPasswordManagementRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasPasswordManagementRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasPasswordManagementRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(CaptchaActivationStrategy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(PasswordChangeRequest.class).test(hints));
    }
}
