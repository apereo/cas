package org.apereo.cas.audit;

/**
 * This is {@link AuditableExecution}. This is a strategy interface which acts as a façade to be used by
 * upstream components that need to call into APIs that require auditing and yet are not managed by Spring.
 * <p>
 * Implementors of this API are used as Spring-managed beans injected into their consumers thus eligible
 * for Spring container services like AOP proxies for audits, transactions, etc.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuditableExecution {

    /**
     * Execute auditable action.
     *
     * @param context the context
     * @return the result
     */
    AuditableExecutionResult execute(AuditableContext context);
}
