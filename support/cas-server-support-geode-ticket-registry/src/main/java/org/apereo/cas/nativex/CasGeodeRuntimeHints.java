package org.apereo.cas.nativex;

import org.apereo.cas.ticket.registry.GeodeTicketDocument;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.geode.internal.cache.InternalCacheBuilder;
import org.apache.geode.logging.internal.log4j.api.LogService;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import java.lang.reflect.Method;

/**
 * This is {@link CasGeodeRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class CasGeodeRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, LogService.class);
        registerSerializationHints(hints, GeodeTicketDocument.class);
        registerSerializationHints(hints, InternalCacheBuilder.class);
    }
}
