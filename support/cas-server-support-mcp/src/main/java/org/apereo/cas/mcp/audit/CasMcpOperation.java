package org.apereo.cas.mcp.audit;

import module java.base;
import org.apereo.cas.audit.AuditableEntity;

/**
 * This is {@link CasMcpOperation}, one call an MCP client made to a CAS MCP tool or resource, as recorded in the
 * CAS audit trail.
 *
 * @author Misagh Moayyed
 * @param operator  the authenticated MCP user who made the call
 * @param operation the tool name, or the URI template of the resource
 * @param arguments the arguments of the call, by name
 * @param outcome   how the call ended
 * @since 8.1.0
 */
public record CasMcpOperation(String operator, String operation, Map<String, String> arguments, Outcome outcome)
    implements AuditableEntity {

    @Override
    public String getAuditablePrincipal() {
        return operator;
    }

    /**
     * How a call ended.
     */
    public enum Outcome {
        /**
         * The call completed.
         */
        SUCCESS,
        /**
         * The caller was not allowed to make the call.
         */
        DENIED,
        /**
         * The call failed with an error.
         */
        FAILED
    }
}
