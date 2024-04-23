package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.flow.WsFedClient;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link WsFederationAuthenticationWebflowRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WsFederationAuthenticationWebflowRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, WsFedClient.class);
    }
}
