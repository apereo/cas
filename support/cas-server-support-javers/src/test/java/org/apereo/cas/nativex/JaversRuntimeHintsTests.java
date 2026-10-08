package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.javers.spring.mongodb.DBRefUnproxyObjectAccessHook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JaversRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class JaversRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JaversRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(
            DBRefUnproxyObjectAccessHook.class.getConstructor()).test(hints));
    }
}
