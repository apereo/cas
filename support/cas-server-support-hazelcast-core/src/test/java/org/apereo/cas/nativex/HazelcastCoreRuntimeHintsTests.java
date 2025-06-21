package org.apereo.cas.nativex;

import com.hazelcast.spi.properties.ClusterProperty;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Native")
class HazelcastCoreRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new HazelcastCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(ClusterProperty.class).test(hints));
    }
}
