package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.configuration.support.JpaPersistenceUnitProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJwksJpaRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class OidcJwksJpaRuntimeHintsTests {

    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new OidcJwksJpaRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(JpaPersistenceUnitProvider.class).test(hints));
    }
}
