package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.storage.generic.JpaMultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.storage.oracle.OracleJpaMultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * Retains trusted-device entity mappings for Hibernate in native images.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class JpaMultifactorAuthenticationTrustRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            MultifactorAuthenticationTrustRecord.class,
            JpaMultifactorAuthenticationTrustRecord.class,
            OracleJpaMultifactorAuthenticationTrustRecord.class));
    }
}
