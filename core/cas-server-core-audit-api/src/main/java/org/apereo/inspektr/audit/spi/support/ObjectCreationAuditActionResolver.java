package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.annotation.Audit;
import org.aspectj.lang.JoinPoint;

/**
 * Uses the success/failure suffixes when an object is returned (or NULL is returned).
 *
 * @author Scott Battaglia
 * @since 1.0
 */
public class ObjectCreationAuditActionResolver extends AbstractSuffixAwareAuditActionResolver {

    public ObjectCreationAuditActionResolver(final String successSuffix, final String failureSuffix) {
        super(successSuffix, failureSuffix);
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Object returnValue, final Audit audit) {
        return audit.action() + (returnValue == null ? getFailureSuffix() : getSuccessSuffix());
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Exception exception, final Audit audit) {
        return audit.action() + getFailureSuffix();
    }
}
