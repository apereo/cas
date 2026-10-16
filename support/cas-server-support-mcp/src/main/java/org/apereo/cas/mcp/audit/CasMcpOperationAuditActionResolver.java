package org.apereo.cas.mcp.audit;

import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.aspectj.lang.JoinPoint;

/**
 * This is {@link CasMcpOperationAuditActionResolver}, which appends the outcome of an MCP call to the audit action,
 * such as {@code MCP_TOOL_CALL_DENIED}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasMcpOperationAuditActionResolver implements AuditActionResolver {
    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Object returnValue, final Audit audit) {
        val outcome = returnValue instanceof final CasMcpOperation operation
            ? operation.outcome()
            : CasMcpOperation.Outcome.FAILED;
        return audit.action() + '_' + outcome.name();
    }

    @Override
    public String resolveFrom(final JoinPoint auditableTarget, final Exception exception, final Audit audit) {
        return audit.action() + '_' + CasMcpOperation.Outcome.FAILED.name();
    }
}
