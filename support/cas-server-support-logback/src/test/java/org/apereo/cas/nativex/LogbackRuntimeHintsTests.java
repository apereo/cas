package org.apereo.cas.nativex;

import module java.base;
import ch.qos.logback.core.ConsoleAppender;
import lombok.val;
import org.fusesource.jansi.AnsiConsole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogbackRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class LogbackRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new LogbackRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(ConsoleAppender.class.getMethod("setWithJansi", boolean.class)).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(AnsiConsole.class.getDeclaredMethod("out")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(AnsiConsole.class.getDeclaredMethod("err")).test(hints));
    }
}
