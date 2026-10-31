package org.apereo.cas.configuration.model.support.mcp;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link McpServerProperties}, the settings of the CAS MCP server.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-mcp")
@Getter
@Setter
@Accessors(chain = true)
public class McpServerProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4127315046839711021L;

    /**
     * Access to the MCP server with OAuth access tokens issued by CAS.
     */
    @NestedConfigurationProperty
    private McpOAuthProperties oauth = new McpOAuthProperties();
}
