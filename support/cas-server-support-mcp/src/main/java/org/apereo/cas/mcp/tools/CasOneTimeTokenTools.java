package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
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
 * This is {@link CasOneTimeTokenTools}, available when one-time password multifactor authentication, such as
 * Google Authenticator, is included in the build. Secret keys, validation codes and scratch codes are never returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasOneTimeTokenTools {
    private final ObjectProvider<OneTimeTokenCredentialRepository> credentialRepository;

    /**
     * Find the one-time password devices of a person. Secret keys, validation and scratch codes are never returned.
     *
     * @param username the username
     * @return the registered devices, newest first
     */
    @McpTool(description = "Find the one-time password devices, such as Google Authenticator, a person has registered for "
        + "multifactor authentication, newest first: the device name, when it was registered and last used, where the "
        + "registration came from and how many scratch codes are left. Secret keys and codes are never returned.",
        title = "Find One-Time Password Devices",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public OneTimeTokenAccounts findOneTimeTokenAccounts(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val repository = credentialRepository.getIfAvailable();
        if (repository == null) {
            return new OneTimeTokenAccounts(username, 0, List.of(), "One-time password multifactor authentication is not configured");
        }
        val accounts = repository.get(username)
            .stream()
            .sorted(Comparator.comparing(OneTimeTokenAccount::getRegistrationDate,
                Comparator.nullsLast(Comparator.<ZonedDateTime>reverseOrder())))
            .map(CasOneTimeTokenTools::summarize)
            .toList();
        return new OneTimeTokenAccounts(username, accounts.size(), accounts, null);
    }

    private static OneTimeTokenDevice summarize(final OneTimeTokenAccount account) {
        return new OneTimeTokenDevice(account.getId(), account.getName(), String.valueOf(account.getRegistrationDate()),
            account.getLastUsedDateTime(), account.getSource(), account.getTenant(),
            account.getScratchCodes() != null ? account.getScratchCodes().size() : 0);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OneTimeTokenAccounts(String username, int count, List<OneTimeTokenDevice> accounts, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OneTimeTokenDevice(long id, @Nullable String name, String registrationDate, @Nullable String lastUsed,
                                     @Nullable String source, @Nullable String tenant, int scratchCodesLeft) {
    }
}
