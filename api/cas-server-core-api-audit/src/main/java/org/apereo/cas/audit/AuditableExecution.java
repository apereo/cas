package org.apereo.cas.audit;

/**
 * This is {@link AuditableExecution}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuditableExecution {

    /**
     * Execute auditable action.
     *
     * @param parameters the parameters
     * @return the result
     */
    AuditableExecutionResult execute(Object... parameters);
}
