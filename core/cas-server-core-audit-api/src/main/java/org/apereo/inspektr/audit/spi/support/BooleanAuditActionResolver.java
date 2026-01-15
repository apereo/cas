package org.apereo.inspektr.audit.spi.support;

import module java.base;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Implementation of {@link AuditActionResolver} that can process boolean return values.
 * Return values are basically action + either the success or failure suffix based on the boolean
 * value.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
public class BooleanAuditActionResolver extends AbstractSuffixAwareAuditActionResolver {

    public BooleanAuditActionResolver(final String successSuffix, final String failureSuffix) {
        super(successSuffix, failureSuffix);
    }


    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Object returnValue, final Audit audit) {
        return audit.action() + ((Boolean) returnValue ? getSuccessSuffix() : getFailureSuffix());
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Exception exception, final Audit audit) {
        return audit.action() + getFailureSuffix();
    }
}
