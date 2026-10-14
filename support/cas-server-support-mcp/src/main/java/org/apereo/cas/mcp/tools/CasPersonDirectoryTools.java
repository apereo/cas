package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasPersonDirectoryTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasPersonDirectoryTools {
    private final PrincipalResolver principalResolver;

    /**
     * Resolve a person and their directory attributes.
     *
     * @param username the username
     * @return the resolved principal
     * @throws Throwable the throwable
     */
    @McpTool(description = "Look up a person from the attribute repository store")
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable Principal findPerson(
        @McpToolParam(description = "The username of the person to look up") final String username)
            throws Throwable {
        return principalResolver.resolve(new BasicIdentifiableCredential(username));
    }
}
