package org.apereo.cas.nativex;

import org.apereo.cas.services.JpaRegisteredServiceEntity;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaServiceRegistryRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Native")
class JpaServiceRegistryRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new JpaServiceRegistryRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(JpaRegisteredServiceEntity.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(JpaRegisteredServiceEntity.class).test(hints));
    }
}
