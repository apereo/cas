package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.adaptors.yubikey.JpaYubiKeyAccount;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaYubiKeyRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class JpaYubiKeyRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JpaYubiKeyRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(JpaYubiKeyAccount.class.getDeclaredConstructor()).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onFieldAccess(JpaYubiKeyAccount.class.getDeclaredField("id")).test(hints));
    }
}
