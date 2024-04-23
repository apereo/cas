package org.apereo.cas.nativex;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link PasswordlessAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class PasswordlessAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(PasswordlessUserAccount.class));
        registerSerializationHints(hints, List.of(PasswordlessUserAccount.class));
    }

}
