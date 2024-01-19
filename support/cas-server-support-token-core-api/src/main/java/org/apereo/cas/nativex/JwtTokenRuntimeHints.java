package org.apereo.cas.nativex;

import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link JwtTokenRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JwtTokenRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            JwtTicketCipherExecutor.class
        ));
    }
}

