package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.GcConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class GitCoreRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new GitCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(JGitText.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethod(GcConfig.PackRefsMode.class.getMethod("values")).invoke().test(hints));
    }
}
