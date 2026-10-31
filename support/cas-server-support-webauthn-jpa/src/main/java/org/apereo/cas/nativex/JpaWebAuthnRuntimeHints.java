package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRegistration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JpaWebAuthnRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class JpaWebAuthnRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, JpaWebAuthnCredentialRegistration.class);
    }
}
