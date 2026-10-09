package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.util.ClassUtils;

/**
 * This is {@link AzureMapsGeoLocationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class AzureMapsGeoLocationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        List.of("com.azure.maps.geolocation.implementation.GeolocationsImpl$GeolocationsService",
            "com.azure.maps.search.implementation.SearchesImpl$SearchesService").forEach(service -> {
                val type = ClassUtils.resolveClassName(service, classLoader);
                hints.proxies().registerJdkProxy(type);
                hints.reflection().registerType(type, MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS);
            });
    }
}
