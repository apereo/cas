package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.cipher.JsonWebKeySetStringCipherExecutor;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.FactoryBean;
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
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CasCoreUtilRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ComponentSerializationPlanConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(Supplier.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(FactoryBean.class).test(hints));

        assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(Object.class.getConstructor()).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(ZonedDateTime.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(LinkedHashMap.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(TreeSet.class).test(hints));

        assertTrue(RuntimeHintsPredicates.reflection().onType(Map.Entry.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(Map.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(Module.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(Class.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(JsonWebKeySetStringCipherExecutor.class).test(hints));
    }

    @Test
    void verifyConcurrentMapEntryHints() throws Exception {
        val hints = new RuntimeHints();
        new CasCoreUtilRuntimeHints().registerHints(hints, getClass().getClassLoader());
        val entry = new ConcurrentHashMap<>(Map.of("key", "value")).entrySet().iterator().next();
        assertTrue(RuntimeHintsPredicates.reflection().onMethod(entry.getClass().getMethod("getKey")).invoke().test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethod(entry.getClass().getMethod("getValue")).invoke().test(hints));
    }
}
