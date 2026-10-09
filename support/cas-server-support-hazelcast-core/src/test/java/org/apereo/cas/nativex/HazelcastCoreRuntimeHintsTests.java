package org.apereo.cas.nativex;

import module java.base;
import com.hazelcast.spi.properties.ClusterProperty;
import com.hazelcast.sql.impl.type.QueryDataType;
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
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new HazelcastCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(Objects.class.getMethod("equals", Object.class, Object.class)).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(ClusterProperty.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onFieldAccess(QueryDataType.class.getDeclaredField("VARCHAR")).test(hints));
        for (val name : List.of("CachedDataRecordWithStats", "CachedSimpleRecord")) {
            val type = Class.forName("com.hazelcast.map.impl.record." + name, false, getClass().getClassLoader());
            assertTrue(RuntimeHintsPredicates.reflection().onFieldAccess(type.getDeclaredField("cachedValue")).test(hints));
        }
    }
}
