package org.apereo.cas.nativex;

import org.apereo.cas.util.cipher.JsonWebKeySetStringCipherExecutor;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreUtilRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasCoreUtilRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasCoreUtilRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ComponentSerializationPlanConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(Supplier.class).test(hints));

        assertTrue(RuntimeHintsPredicates.serialization().onType(ZonedDateTime.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(LinkedHashMap.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(TreeSet.class).test(hints));

        assertTrue(RuntimeHintsPredicates.reflection().onType(Map.Entry.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(Map.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(Module.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(Class.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(JsonWebKeySetStringCipherExecutor.class).test(hints));
    }
}
