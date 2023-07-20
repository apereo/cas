package org.apereo.cas.services;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategyEnforcer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyRegisteredServiceAccessStrategyEnforcer implements RegisteredServiceAccessStrategyEnforcer, DisposableBean {
    private final WatchableGroovyScriptResource script;

    @Override
    public AuditableExecutionResult execute(final AuditableContext context) {
        val args = new Object[]{context, LOGGER};
        return script.execute(args, AuditableExecutionResult.class, true);
    }

    @Override
    public void destroy() {
        script.close();
    }
}
