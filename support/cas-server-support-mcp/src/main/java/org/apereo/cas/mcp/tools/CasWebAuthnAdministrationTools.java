package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasWebAuthnAdministrationTools}, available when WebAuthn multifactor authentication is included in the
 * build and the {@code mcp-write} feature is turned on.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasWebAuthnAdministrationTools {
    private final ObjectProvider<WebAuthnCredentialRepository> webAuthnCredentialRepository;

    /**
     * Remove the WebAuthn devices of a person.
     *
     * @param username the username
     * @return the number of removed devices
     */
    @McpTool(description = "Remove all WebAuthn security keys and passkeys a person has registered for multifactor "
        + "authentication, so the person registers a new one at the next login. Use findWebAuthnDevices first.",
        title = "Remove WebAuthn Devices",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public RemovedWebAuthnDevices removeWebAuthnDevices(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val repository = webAuthnCredentialRepository.getIfAvailable();
        if (repository == null) {
            return new RemovedWebAuthnDevices(username, 0, "WebAuthn multifactor authentication is not configured");
        }
        val devices = repository.getRegistrationsByUsername(username).size();
        repository.removeAllRegistrations(username);
        return new RemovedWebAuthnDevices(username, devices, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RemovedWebAuthnDevices(String username, int removed, @Nullable String reason) {
    }
}
