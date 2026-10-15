package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasPasswordlessTools}, available when passwordless authentication is included in the build.
 * Email addresses and phone numbers are masked, and account attributes are not returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasPasswordlessTools {
    private static final int PHONE_DIGITS_SHOWN = 2;

    private final ObjectProvider<PasswordlessUserAccountStore> passwordlessUserAccountStore;

    /**
     * Find the passwordless account of a person.
     *
     * @param username the username
     * @return the passwordless account, with contact details masked
     */
    @McpTool(description = "Find the passwordless authentication account of a person: whether it can receive tokens by "
        + "email or text message (with the address and number masked), whether the person must still enter a password, "
        + "may pick from a selection menu, is eligible for multifactor or delegated authentication and which identity "
        + "providers it may use, and where the account comes from.",
        title = "Find Passwordless Account",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findPasswordlessAccount(
        @McpToolParam(description = "The username of the person")
        final String username) {
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        val store = passwordlessUserAccountStore.getIfAvailable();
        if (store == null) {
            result.put("reason", "Passwordless authentication is not configured");
            return result;
        }
        val request = PasswordlessAuthenticationRequest.builder().username(username).build();
        val account = FunctionUtils.<Optional<? extends PasswordlessUserAccount>>doUnchecked(() -> store.findUser(request));
        account.ifPresentOrElse(found -> result.put("account", summarize(found)),
            () -> result.put("reason", "No passwordless account is found for " + username));
        return result;
    }

    private static Map<String, Object> summarize(final PasswordlessUserAccount account) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("username", account.getUsername());
        summary.put("name", account.getName());
        summary.put("email", maskEmail(account.getEmail()));
        summary.put("phone", maskPhone(account.getPhone()));
        summary.put("requestPassword", account.isRequestPassword());
        summary.put("allowSelectionMenu", account.isAllowSelectionMenu());
        summary.put("multifactorAuthenticationEligible", String.valueOf(account.getMultifactorAuthenticationEligible()));
        summary.put("delegatedAuthenticationEligible", String.valueOf(account.getDelegatedAuthenticationEligible()));
        summary.put("allowedDelegatedClients", account.getAllowedDelegatedClients());
        summary.put("source", account.getSource());
        return summary;
    }

    private static @Nullable String maskEmail(final @Nullable String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        val at = email.indexOf('@');
        if (at <= 0) {
            return "*".repeat(email.length());
        }
        return email.charAt(0) + "*".repeat(Math.max(1, at - 1)) + email.substring(at);
    }

    private static @Nullable String maskPhone(final @Nullable String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        val shown = Math.min(PHONE_DIGITS_SHOWN, phone.length());
        return "*".repeat(phone.length() - shown) + phone.substring(phone.length() - shown);
    }
}
