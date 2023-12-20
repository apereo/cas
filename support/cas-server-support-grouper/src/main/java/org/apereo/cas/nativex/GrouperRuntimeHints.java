package org.apereo.cas.nativex;

import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link GrouperRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class GrouperRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, GrouperFacade.class);
    }
}
