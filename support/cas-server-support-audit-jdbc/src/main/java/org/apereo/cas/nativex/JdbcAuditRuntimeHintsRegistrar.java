package org.apereo.cas.nativex;

import org.apereo.cas.audit.spi.entity.AuditTrailEntity;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link JdbcAuditRuntimeHintsRegistrar}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JdbcAuditRuntimeHintsRegistrar implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(AuditTrailEntity.class));
    }
}
