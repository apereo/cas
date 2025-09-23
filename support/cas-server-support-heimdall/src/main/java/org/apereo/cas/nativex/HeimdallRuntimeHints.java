package org.apereo.cas.nativex;

import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link HeimdallRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class HeimdallRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, AuthorizableResourceRepository.class);
    }
}

