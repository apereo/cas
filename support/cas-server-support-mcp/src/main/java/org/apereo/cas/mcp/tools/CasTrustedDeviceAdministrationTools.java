package org.apereo.cas.mcp.tools;

import module java.base;
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
 * This is {@link CasTrustedDeviceAdministrationTools}, available when multifactor authentication trusted devices are
 * included in the build and the {@code mcp-write} feature is turned on.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasTrustedDeviceAdministrationTools {
    private final ObjectProvider<MultifactorAuthenticationTrustStorage> trustStorage;

    /**
     * Remove the trusted devices of a person.
     *
     * @param username the username
     * @param deviceId the id of a single device, optional
     * @return the number of removed devices
     */
    @McpTool(description = "Remove the devices a person has registered as trusted for multifactor authentication, or a "
        + "single device given its id from findTrustedDevices, so CAS asks for multifactor authentication again on those "
        + "devices. Use it when a device is lost or should no longer be trusted.",
        title = "Remove Trusted Devices",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public RemovedTrustedDevices removeTrustedDevices(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username,
        @McpToolParam(description = "Only remove the device with this id, as returned by findTrustedDevices", required = false)
        @Nullable final Long deviceId) {
        val storage = trustStorage.getIfAvailable();
        if (storage == null) {
            return new RemovedTrustedDevices(username, 0, "Multifactor authentication trusted devices are not configured");
        }
        val devices = storage.get(username)
            .stream()
            .filter(device -> deviceId == null || deviceId.equals(device.getId()))
            .toList();
        devices.forEach(device -> storage.remove(device.getRecordKey()));
        return new RemovedTrustedDevices(username, devices.size(),
            devices.isEmpty() && deviceId != null ? "No trusted device of the person has this id" : null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RemovedTrustedDevices(String username, int removed, @Nullable String reason) {
    }
}
