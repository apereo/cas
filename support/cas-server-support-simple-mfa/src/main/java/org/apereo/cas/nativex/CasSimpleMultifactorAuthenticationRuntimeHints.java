package org.apereo.cas.nativex;

import org.apereo.cas.bucket4j.producer.BucketStore;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CasSimpleMultifactorAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasSimpleMultifactorAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, List.of(BucketStore.class));
    }
}
