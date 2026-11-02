package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.registry.PulsarTicketRegistryMessageListener;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apache.pulsar.client.impl.ConsumerBase;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

/**
 * This is {@link PulsarTicketRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class PulsarTicketRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.reflection().registerType(PulsarTicketRegistryMessageListener.class,
            hint -> hint.withMethod("receive", List.of(TypeReference.of(String.class)), ExecutableMode.INVOKE));
        hints.reflection().registerType(ConsumerBase.class, hint -> hint.withField("conf"));
    }
}
