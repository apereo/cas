package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * This is {@link CasEventsRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class CasEventsRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSpringProxyHints(hints, CasEventRepository.class, ApplicationEventPublisherAware.class);
        registerSerializationHints(hints, findSubclassesOf(AbstractCasEvent.class));
    }
}
