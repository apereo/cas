package org.apereo.cas.nativex;

import org.apereo.cas.configuration.model.core.web.view.CustomLoginFieldViewProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreConfigurationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasCoreConfigurationRuntimeHintsTests {

    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasCoreConfigurationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(CustomLoginFieldViewProperties.class).test(hints));
    }
}

