package org.apereo.cas.nativex;

import org.apereo.cas.ticket.registry.HazelcastTicketDocument;
import org.apereo.cas.ticket.registry.MapAttributeValueExtractor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link HazelcastTicketRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class HazelcastTicketRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(MapAttributeValueExtractor.class));
        registerSerializationHints(hints, List.of(HazelcastTicketDocument.class));
    }

}
