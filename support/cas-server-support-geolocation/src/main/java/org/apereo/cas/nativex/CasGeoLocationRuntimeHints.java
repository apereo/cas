package org.apereo.cas.nativex;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasGeoLocationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasGeoLocationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, GeoLocationService.class);
    }
}
