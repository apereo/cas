package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link DelegatedClientsRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DelegatedClientsRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerPattern("META-INF/services/com.fasterxml.jackson.databind.Module");
        registerProxyHints(hints, DelegatedClientFactoryCustomizer.class, DelegatedIdentityProviderFactory.class);
        registerReflectionHints(hints, findSubclassesInPackage(DelegatedIdentityProviderFactory.class, CentralAuthenticationService.NAMESPACE));
    }
}
