package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryInitializer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreServicesRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasCoreServicesRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasCoreServicesRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ServiceRegistryInitializer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ServiceRegistry.class).test(hints));

        assertTrue(RuntimeHintsPredicates.serialization().onType(CasRegisteredService.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(BaseRegisteredService.class).test(hints));
    }
}
