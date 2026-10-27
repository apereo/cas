package org.apereo.cas.configuration.model.support.mcp;

import module java.base;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link McpOAuthProperties}, which controls access to the MCP server with OAuth access tokens issued by CAS.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-mcp")
@Getter
@Setter
@Accessors(chain = true)
public class McpOAuthProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2693071128814930527L;

    /**
     * Principal attributes that the person behind an access token must have to be granted the {@code MCP_ADMIN} role,
     * keyed by attribute name, with a regular expression as the value. Every attribute must be present with at least
     * one value that fully matches its expression. When no attributes are defined, nobody is granted the role with an
     * access token: such callers may connect and list tools, but every tool call is refused.
     */
    @RegularExpressionCapable
    private Map<String, String> requiredAttributes = new LinkedHashMap<>();

    /**
     * Principal attributes that the person behind an access token must have to be granted the {@code MCP_OPERATOR} role,
     * which the tools that change CAS require, keyed by attribute name, with a regular expression as the value. Every
     * attribute must be present with at least one value that fully matches its expression, and the token must also carry
     * the {@code mcp:write} scope. When no attributes are defined, nobody is granted the role with an access token.
     */
    @RegularExpressionCapable
    private Map<String, String> operatorRequiredAttributes = new LinkedHashMap<>();
}
