package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintExtractor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link MultifactorAuthenticationTrustedHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class MultifactorAuthenticationTrustedHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints, MultifactorAuthenticationTrustRecord.class);
        registerProxyHints(hints, DeviceFingerprintExtractor.class,
            MultifactorAuthenticationTrustStorage.class,
            MultifactorAuthenticationTrustRecordKeyGenerator.class);
    }
}

