package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.transport.HttpConfig;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link GitServiceRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuppressWarnings("removal")
public class GitServiceRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            HttpConfig.HttpRedirectMode.class,
            DirCache.DirCacheVersion.class,
            JGitText.class,
            CoreConfig.CheckStat.class,
            CoreConfig.SymLinks.class,
            CoreConfig.HideDotFiles.class,
            CoreConfig.LogRefUpdates.class,
            CoreConfig.EolStreamType.class,
            CoreConfig.EOL.class,
            CoreConfig.TrustPackedRefsStat.class,
            CoreConfig.TrustLooseRefStat.class,
            CoreConfig.TrustStat.class)
        );
    }
}
