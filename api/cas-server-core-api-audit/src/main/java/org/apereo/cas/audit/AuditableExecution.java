package org.apereo.cas.audit;

/**
 * This is {@link AuditableExecution}. This is a strategy interface which acts as an abstraction for audit trail execution
 * at various audit points throughout CAS server and its various modules.
 * <p>
 * Implementers of this API are typically annotated with <i>Inspektr's</i> auditing library {@code Audit} annotation
 * to capture data at any particular audit point and make it available to main Inspektr's engine for processing that audit trail data.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuditableExecution {

    /**
     * Auditable enforcer to check registered service access.
     */
    String AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS = "registeredServiceAccessStrategyEnforcer";

    /**
     * Auditable enforcer to check registered service access for delegated authentication policy.
     */
    String AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS = "registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer";

    /**
     * Execute auditable action.
     *
     * @param context the context
     * @return the result
     * @throws Throwable the throwable
     */
    AuditableExecutionResult execute(AuditableContext context) throws Throwable;
}
