package org.apereo.cas.nativex;

import lombok.val;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InweboAuthenticationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class InweboAuthenticationRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new InweboAuthenticationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(XIncludeAwareParserConfiguration.class).test(hints));
    }
}
