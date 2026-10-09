package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JavaMelodyRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class JavaMelodyRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JavaMelodyRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.resource().forBundle("net.bull.javamelody.resource.translations").test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forResource("net/bull/javamelody/resource/monitoring.css").test(hints));
        val operatingSystem = Class.forName("com.sun.management.OperatingSystemMXBean");
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(operatingSystem.getDeclaredMethod("getTotalPhysicalMemorySize")).test(hints));
        val unixOperatingSystem = Class.forName("com.sun.management.UnixOperatingSystemMXBean");
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(unixOperatingSystem.getDeclaredMethod("getOpenFileDescriptorCount")).test(hints));
        val counter = Class.forName("net.bull.javamelody.internal.model.Counter");
        val controller = Class.forName("net.bull.javamelody.internal.web.HtmlController");
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(controller.getDeclaredMethod("doCounterSummaryPerClass", String.class, String.class)).test(hints));
        assertTrue(hints.reflection().getTypeHint(counter).hasJavaSerialization());
        val counterRequest = Class.forName("net.bull.javamelody.internal.model.CounterRequest");
        assertTrue(hints.reflection().getTypeHint(counterRequest).hasJavaSerialization());
        val type = Class.forName("com.sun.management.ThreadMXBean");
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(type.getDeclaredMethod("isThreadAllocatedMemorySupported")).test(hints));
    }
}
