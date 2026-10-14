package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasTrustedDeviceTools}, available when multifactor authentication trusted devices are included in the build.
 * Device fingerprints and record keys are never returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasTrustedDeviceTools {
    private final ObjectProvider<MultifactorAuthenticationTrustStorage> trustStorage;

    /**
     * Find the trusted devices of a person.
     *
     * @param username the username
     * @return the trusted device records, newest first
     */
    @McpTool(description = "Find the devices a person has registered as trusted for multifactor authentication, newest first: "
        + "the device name, the multifactor authentication provider, when it was registered, when it expires and whether "
        + "it has expired. Device fingerprints are never returned.",
        title = "Find Trusted Devices",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findTrustedDevices(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        val storage = trustStorage.getIfAvailable();
        if (storage == null) {
            result.put("reason", "Multifactor authentication trusted devices are not configured");
            return result;
        }
        val devices = storage.get(username)
            .stream()
            .sorted(Comparator.comparing(MultifactorAuthenticationTrustRecord::getRecordDate,
                Comparator.nullsLast(Comparator.<ZonedDateTime>reverseOrder())))
            .map(CasTrustedDeviceTools::summarize)
            .toList();
        result.put("count", devices.size());
        result.put("devices", devices);
        return result;
    }

    private static Map<String, Object> summarize(final MultifactorAuthenticationTrustRecord record) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", record.getId());
        summary.put("name", record.getName());
        summary.put("multifactorAuthenticationProvider", record.getMultifactorAuthenticationProvider());
        summary.put("recordDate", String.valueOf(record.getRecordDate()));
        summary.put("expirationDate", record.getExpirationDate() != null ? record.getExpirationDate().toInstant().toString() : null);
        summary.put("expired", record.isExpired());
        return summary;
    }
}
