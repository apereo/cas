package org.apereo.cas.audit;

/**
 * This is {@link BaseAuditableExecution}. Provides a simple implementation that just copies data from context to execution result.
 * Useful for subclasses with simple requirements that just need to capture context data at audit points and pass it on as a result.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public abstract class BaseAuditableExecution implements AuditableExecution {

    @Override
    public AuditableExecutionResult execute(final AuditableContext context) {
        return AuditableExecutionResult.of(context);
    }
}
