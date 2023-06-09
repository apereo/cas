package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link Pac4jCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class Pac4jCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.serialization()
            .registerType(BasicUserProfile.class)
            .registerType(CommonProfile.class);
    }
}
