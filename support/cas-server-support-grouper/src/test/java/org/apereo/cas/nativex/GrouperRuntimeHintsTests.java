package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.grouper.GrouperFacade;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GrouperRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class GrouperRuntimeHintsTests {

    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new GrouperRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(GrouperFacade.class).test(hints));
    }
}
