package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
     * Find the one-time password devices a person has registered.
     *
     * @param username the username
     * @return the registered devices, newest first
     */
    @McpTool(description = "Find the one-time password devices, such as Google Authenticator, a person has registered for "
        + "multifactor authentication, newest first: the device name, when it was registered and last used, where the "
        + "registration came from and how many scratch codes are left. Secret keys and codes are never returned.",
        title = "Find One-Time Password Devices",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findOneTimeTokenAccounts(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        val repository = credentialRepository.getIfAvailable();
        if (repository == null) {
            result.put("reason", "One-time password multifactor authentication is not configured");
            return result;
        }
        val accounts = repository.get(username)
            .stream()
            .sorted(Comparator.comparing(OneTimeTokenAccount::getRegistrationDate,
                Comparator.nullsLast(Comparator.<ZonedDateTime>reverseOrder())))
            .map(CasOneTimeTokenTools::summarize)
            .toList();
        result.put("count", accounts.size());
        result.put("accounts", accounts);
        return result;
    }

    private static Map<String, Object> summarize(final OneTimeTokenAccount account) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", account.getId());
        summary.put("name", account.getName());
        summary.put("registrationDate", String.valueOf(account.getRegistrationDate()));
        summary.put("lastUsed", account.getLastUsedDateTime());
        summary.put("source", account.getSource());
        summary.put("tenant", account.getTenant());
        summary.put("scratchCodesLeft", account.getScratchCodes() != null ? account.getScratchCodes().size() : 0);
        return summary;
    }
}
