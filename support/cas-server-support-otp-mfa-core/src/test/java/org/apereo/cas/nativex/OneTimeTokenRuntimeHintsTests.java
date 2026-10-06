package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OneTimeTokenRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class OneTimeTokenRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new OneTimeTokenRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(OneTimeTokenAccountCipherExecutor.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(OneTimeTokenAccount.class.getConstructor()).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(OneTimeTokenAccount.class.getMethod("getUsername")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(OneTimeTokenAccount.class.getMethod("setUsername", String.class)).test(hints));
        val accountHint = hints.reflection().getTypeHint(OneTimeTokenAccount.class);
        assertNotNull(accountHint);
        assertTrue(accountHint.hasJavaSerialization());
    }
}
