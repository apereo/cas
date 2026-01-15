package org.apereo.inspektr.audit.spi;

import module java.base;
import org.apereo.inspektr.audit.annotation.Audit;
import org.aspectj.lang.JoinPoint;

/**
 * An SPI interface needed to be implemented by individual applications requiring an audit trail record keeping
 * functionality, to provide the action taken.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
public interface AuditActionResolver {

    /**
     * Resolve the action for the audit event.
     *
     * @param auditableTarget the auditable target
     * @param returnValue          The returned value
     * @param audit           the Audit annotation that may contain additional information.
     * @return The resource String
     */
    String resolveFrom(JoinPoint auditableTarget, Object returnValue, Audit audit);

    /**
     * Resolve the action for the audit event that has incurred
     * an exception.
     *
     * @param auditableTarget the auditable target
     * @param exception       The exception incurred when the join point proceeds.
     * @param audit           the Audit annotation that may contain additional information.
     * @return The resource String
     */
    String resolveFrom(JoinPoint auditableTarget, Exception exception, Audit audit);

}
