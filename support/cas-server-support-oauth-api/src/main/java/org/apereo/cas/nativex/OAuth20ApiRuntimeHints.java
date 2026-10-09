package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link OAuth20ApiRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OAuth20ApiRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, OAuth20AuthorizationRequestValidator.class);
    }
}
