package org.apereo.cas.mcp.audit;

import org.apereo.inspektr.audit.annotation.Audit;

/**
 * This is {@link CasMcpAuditor}. Calling {@link #record(CasMcpOperation)} hands an MCP call to the CAS audit trail through
 * the regular {@link Audit} machinery, so the record goes wherever CAS audit records go and is subject to the same
 * audit settings.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasMcpAuditor {
    /**
     * The audit action of an MCP call; the outcome is appended, such as {@code MCP_TOOL_CALL_SUCCESS}.
     */
    public static final String AUDIT_ACTION = "MCP_TOOL_CALL";

    /**
     * The name of the audit action resolver of MCP calls.
     */
    public static final String AUDIT_ACTION_RESOLVER = "MCP_TOOL_CALL_ACTION_RESOLVER";

    /**
     * The name of the audit resource resolver of MCP calls.
     */
    public static final String AUDIT_RESOURCE_RESOLVER = "MCP_TOOL_CALL_RESOURCE_RESOLVER";

    /**
     * Record an MCP call in the audit trail.
     *
     * @param operation the call
     * @return the call
     */
    @Audit(action = AUDIT_ACTION, actionResolverName = AUDIT_ACTION_RESOLVER, resourceResolverName = AUDIT_RESOURCE_RESOLVER)
    public CasMcpOperation record(final CasMcpOperation operation) {
        return operation;
    }
}
