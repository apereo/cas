package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
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
     * Find the trusted devices of a person. Device fingerprints and record keys are never returned.
     *
     * @param username the username
     * @return the trusted devices, newest first
     */
    @McpTool(description = "Find the devices a person has registered as trusted for multifactor authentication, newest first: "
        + "the device name, the multifactor authentication provider, when it was registered, when it expires and whether "
        + "it has expired. Device fingerprints are never returned.",
        title = "Find Trusted Devices",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public TrustedDevices findTrustedDevices(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val storage = trustStorage.getIfAvailable();
        if (storage == null) {
            return new TrustedDevices(username, 0, List.of(), "Multifactor authentication trusted devices are not configured");
        }
        val devices = storage.get(username)
            .stream()
            .sorted(Comparator.comparing(MultifactorAuthenticationTrustRecord::getRecordDate,
                Comparator.nullsLast(Comparator.<ZonedDateTime>reverseOrder())))
            .map(CasTrustedDeviceTools::summarize)
            .toList();
        return new TrustedDevices(username, devices.size(), devices, null);
    }

    private static TrustedDevice summarize(final MultifactorAuthenticationTrustRecord record) {
        return new TrustedDevice(record.getId(), record.getName(), record.getMultifactorAuthenticationProvider(),
            String.valueOf(record.getRecordDate()),
            record.getExpirationDate() != null ? record.getExpirationDate().toInstant().toString() : null,
            record.isExpired());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TrustedDevices(String username, int count, List<TrustedDevice> devices, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TrustedDevice(long id, @Nullable String name, @Nullable String multifactorAuthenticationProvider, String recordDate,
                                @Nullable String expirationDate, boolean expired) {
    }
}
