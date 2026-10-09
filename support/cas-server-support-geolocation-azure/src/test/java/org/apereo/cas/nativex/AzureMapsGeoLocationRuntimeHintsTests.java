package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.util.ClassUtils;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AzureMapsGeoLocationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class AzureMapsGeoLocationRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new AzureMapsGeoLocationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        for (val service : List.of("com.azure.maps.geolocation.implementation.GeolocationsImpl$GeolocationsService",
            "com.azure.maps.search.implementation.SearchesImpl$SearchesService")) {
            val type = ClassUtils.resolveClassName(service, getClass().getClassLoader());
            assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(type).test(hints));
            for (val method : type.getDeclaredMethods()) {
                assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(method).test(hints));
            }
        }
    }
}
