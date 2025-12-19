package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTokenRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class JwtTokenRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new JwtTokenRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(JwtTicketCipherExecutor.class).test(hints));
    }
}
