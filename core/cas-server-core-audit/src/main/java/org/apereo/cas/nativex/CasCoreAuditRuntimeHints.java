package org.apereo.cas.nativex;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasCoreAuditRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreAuditRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies().registerJdkProxy(AuditTrailExecutionPlanConfigurer.class);
    }
}
