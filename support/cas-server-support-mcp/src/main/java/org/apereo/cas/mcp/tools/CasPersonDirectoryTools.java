package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
     * @return the resolved person
     */
    @McpTool(description = "Look up a person and their attributes from the CAS attribute repositories",
        title = "Find Person",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Person findPerson(
        @McpToolParam(description = "The username of the person to look up")
        final String username) {
        val principal = FunctionUtils.doUnchecked(() -> principalResolver.resolve(new BasicIdentifiableCredential(username)));
        if (principal == null || principal instanceof NullPrincipal) {
            return new Person(username, Map.of(), "No person is found for " + username);
        }
        return new Person(principal.getId(), new TreeMap<>(principal.getAttributes()), null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Person(String id, Map<String, List<Object>> attributes, @Nullable String reason) {
    }
}
