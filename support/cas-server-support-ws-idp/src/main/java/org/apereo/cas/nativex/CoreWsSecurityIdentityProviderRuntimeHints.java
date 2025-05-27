package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CoreWsSecurityIdentityProviderRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CoreWsSecurityIdentityProviderRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, WSFederationRegisteredService.class);
        registerReflectionHints(hints, List.of(
            WSFederationRegisteredService.class
        ));
    }
}
