package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasOneTimeTokenAdministrationTools}, available when one-time password multifactor authentication is
 * included in the build and the {@code mcp-write} feature is turned on.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasOneTimeTokenAdministrationTools {
    private final ObjectProvider<OneTimeTokenCredentialRepository> credentialRepository;

    /**
     * Remove the one-time password devices of a person.
     *
     * @param username  the username
     * @param accountId the id of a single device, optional
     * @return the number of removed devices
     */
    @McpTool(description = "Remove the one-time password devices, such as Google Authenticator, a person has registered for "
        + "multifactor authentication, or a single device given its id from findOneTimeTokenAccounts, so the person "
        + "registers a new device at the next login. Use it when a phone is lost or replaced.",
        title = "Remove One-Time Password Devices",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public RemovedOneTimeTokenAccounts removeOneTimeTokenAccounts(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username,
        @McpToolParam(description = "Only remove the device with this id, as returned by findOneTimeTokenAccounts", required = false)
        @Nullable final Long accountId) {
        val repository = credentialRepository.getIfAvailable();
        if (repository == null) {
            return new RemovedOneTimeTokenAccounts(username, 0, "One-time password multifactor authentication is not configured");
        }
        if (accountId != null) {
            val id = accountId.longValue();
            val found = repository.get(username).stream().anyMatch(account -> account.getId() == id);
            if (found) {
                repository.delete(id);
            }
            return new RemovedOneTimeTokenAccounts(username, found ? 1 : 0,
                found ? null : "No one-time password device of the person has this id");
        }
        val accounts = repository.get(username).size();
        repository.delete(username);
        return new RemovedOneTimeTokenAccounts(username, accounts, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RemovedOneTimeTokenAccounts(String username, int removed, @Nullable String reason) {
    }
}
