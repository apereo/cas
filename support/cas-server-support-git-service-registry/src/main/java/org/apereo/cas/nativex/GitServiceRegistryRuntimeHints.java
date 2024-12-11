package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.CoreConfig;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link GitServiceRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class GitServiceRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(JGitText.class, CoreConfig.TrustLooseRefStat.class));
    }
}
