package org.apereo.cas.nativex;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
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
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSpringProxyHints(hints, CasEventRepository.class, ApplicationEventPublisherAware.class);
        registerSerializationHints(hints, findSubclassesOf(AbstractCasEvent.class));
    }
}
