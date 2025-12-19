package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.annotation.Audit;
import module java.base;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;

/**
 * Default resolver.  If a suffix is defined for success and failure, the failure suffix is appended if an exception is
 * thrown. Otherwise, the success suffix is used.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
public class DefaultAuditActionResolver extends AbstractSuffixAwareAuditActionResolver {

    public DefaultAuditActionResolver() {
        this(StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public DefaultAuditActionResolver(final String successSuffix) {
        this(successSuffix, StringUtils.EMPTY);
    }

    public DefaultAuditActionResolver(final String successSuffix, final String failureSuffix) {
        super(successSuffix, failureSuffix);
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Object returnValue, final Audit audit) {
        return audit.action() + getSuccessSuffix();
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Exception exception, final Audit audit) {
        return audit.action() + getFailureSuffix();
    }
}
