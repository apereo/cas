package org.apereo.cas.nativex;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityPasscodeCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredential;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link DuoSecurityRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DuoSecurityRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints,
            DuoSecurityUniversalPromptCredential.class,
            DuoSecurityPasscodeCredential.class,
            DuoSecurityDirectCredential.class);
        registerSerializationHints(hints,
            DuoSecurityUniversalPromptCredential.class,
            DuoSecurityPasscodeCredential.class,
            DuoSecurityDirectCredential.class);
    }
}
