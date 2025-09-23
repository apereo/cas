package org.apereo.cas.nativex;

import org.apereo.cas.services.web.CasThymeleafTemplatesDirector;
import org.apereo.cas.web.view.CasMustacheView;
import org.apereo.cas.web.view.CasThymeleafView;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasThymeleafRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasThymeleafRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasThymeleafRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.resource().forBundle("messages").test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(CasThymeleafTemplatesDirector.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(CasThymeleafView.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(CasMustacheView.class).test(hints));
    }
}
