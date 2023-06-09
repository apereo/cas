package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.BasicUserProfile;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Pac4jCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
public class Pac4jCoreRuntimeHintsTests {
    @Test
    public void verifyHints() {
        val hints = new RuntimeHints();
        new Pac4jCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(BasicUserProfile.class).test(hints));
    }
}
