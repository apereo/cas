package org.apereo.inspektr.audit.spi;

import module java.base;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * An SPI interface needed to be implemented by individual applications requiring an audit trail record keeping
 * functionality, to provide a current resource on which an audit-able action is being performed.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0
 */
public interface AuditResourceResolver {

    /**
     * Resolve the auditable resource.
     *
     * @param target      the join point that contains the arguments.
     * @param returnValue The returned value
     * @return The resource String.
     */
    @Nullable String[] resolveFrom(JoinPoint target, @Nullable Object returnValue);

    /**
     * Resolve the auditable resource for an audit-able action that has
     * incurred an exception.
     *
     * @param target    the join point that contains the arguments.
     * @param exception The exception incurred when the join point proceeds.
     * @return The resource String.
     */
    @Nullable String[] resolveFrom(JoinPoint target, Exception exception);

    /**
     * Resolve what the action was for, when it is something that can be counted, such as the id of the application
     * signed in to or the multifactor provider that was triggered. Stored apart from the resource.
     *
     * @param target      the join point that contains the arguments
     * @param returnValue the returned value
     * @return the target, or null
     */
    default @Nullable String resolveTarget(final JoinPoint target, @Nullable final Object returnValue) {
        return null;
    }

    /**
     * Resolve what an action that incurred an exception was for.
     *
     * @param target    the join point that contains the arguments
     * @param exception the exception incurred when the join point proceeds
     * @return the target, or null
     */
    default @Nullable String resolveTarget(final JoinPoint target, final Exception exception) {
        return null;
    }

    /**
     * Sets audit format.
     *
     * @param auditFormat the audit format
     */
    default void setAuditFormat(final AuditTrailManager.AuditFormats auditFormat) {
    }
}
