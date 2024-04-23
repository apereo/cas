package org.apereo.cas.nativex;

import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link WsFederationAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WsFederationAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(WsFederationCredentialsToPrincipalResolver.class));
    }

}
