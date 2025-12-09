package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.profile.UserProfile;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link DelegatedAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DelegatedAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        val profiles = findSubclassesInPackage(UserProfile.class, "org.pac4j");
        registerSerializationHints(hints, profiles);
        registerReflectionHints(hints, profiles);

        registerReflectionHints(hints,
            List.of(DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor.class));
    }
}
