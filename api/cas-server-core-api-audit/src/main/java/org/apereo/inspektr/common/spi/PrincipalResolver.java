package org.apereo.inspektr.common.spi;

import module java.base;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * An SPI interface needed to be implemented by individual applications requiring an audit trail record keeping
 * functionality, to provide a currently authenticated principal performing an audit-able action.
 *
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @since 1.0
 */
public interface PrincipalResolver {

    /**
     * Default String that can be used when the user cannot be determined.
     */
    String UNKNOWN_USER = "audit:unknown";

    /**
     * Resolve the principal performing an audit-able action.
     * <p>
     * Note, this method should NEVER throw an exception *unless* the expectation is that a failed resolution causes
     * the entire transaction to fail.  Otherwise use {@link PrincipalResolver#UNKNOWN_USER}.
     *
     * @param auditTarget the join point where we're auditing.
     * @param returnValue the returned value
     * @return The principal as a String. CANNOT be NULL.
     */
    @Nullable String resolveFrom(JoinPoint auditTarget, Object returnValue);

    /**
     * Resolve the principal performing an audit-able action that has incurred
     * an exception.
     * <p>
     * Note, this method should NEVER throw an exception *unless* the expectation is that a failed resolution causes
     * the entire transaction to fail.  Otherwise use {@link PrincipalResolver#UNKNOWN_USER}.
     *
     * @param auditTarget the join point where we're auditing.
     * @param exception   The exception incurred when the join point proceeds.
     * @return The principal as a String. CANNOT be NULL.
     */
    @Nullable String resolveFrom(JoinPoint auditTarget, Exception exception);

    /**
     * Called when there is no other way to resolve the principal (i.e. an error was captured, auditing was not
     * called, etc.)
     * <p>
     * Note, this method should NEVER throw an exception *unless* the expectation is that a failed resolution causes
     * the entire transaction to fail.  Otherwise use {@link PrincipalResolver#UNKNOWN_USER}.
     *
     * @return the principal.  CANNOT be NULL.
     */
    default String resolve() {
        return UNKNOWN_USER;
    }
}
