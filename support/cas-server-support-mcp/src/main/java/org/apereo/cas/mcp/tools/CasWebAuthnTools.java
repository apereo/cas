package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.data.AuthenticatorTransport;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasWebAuthnTools}, available when WebAuthn (FIDO2 and passkey) multifactor authentication is included
 * in the build. Credential ids, user handles and public keys are never returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasWebAuthnTools {
    private final ObjectProvider<WebAuthnCredentialRepository> webAuthnCredentialRepository;

    /**
     * Find the WebAuthn devices and passkeys a person has registered.
     *
     * @param username the username
     * @return the registered devices, newest first
     */
    @McpTool(description = "Find the WebAuthn security keys and passkeys a person has registered, newest first: the "
        + "nickname, when it was registered, the transports it supports, whether it is a discoverable credential (passkey), "
        + "the AAGUID that identifies the authenticator model and its signature count. Credential ids and key material "
        + "are never returned.",
        title = "Find WebAuthn Devices",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findWebAuthnDevices(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        val repository = webAuthnCredentialRepository.getIfAvailable();
        if (repository == null) {
            result.put("reason", "WebAuthn multifactor authentication is not configured");
            return result;
        }
        val devices = repository.getRegistrationsByUsername(username)
            .stream()
            .sorted(Comparator.comparing(CredentialRegistration::getRegistrationTime,
                Comparator.nullsLast(Comparator.<Instant>reverseOrder())))
            .map(CasWebAuthnTools::summarize)
            .toList();
        result.put("count", devices.size());
        result.put("devices", devices);
        return result;
    }

    private static Map<String, Object> summarize(final CredentialRegistration registration) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("nickname", registration.getCredentialNickname());
        summary.put("registrationTime", String.valueOf(registration.getRegistrationTime()));
        val transports = registration.getTransports();
        summary.put("transports", transports != null
            ? transports.stream().map(AuthenticatorTransport::getId).toList()
            : List.of());
        summary.put("discoverable", registration.getDiscoverable());
        summary.put("aaguid", registration.getAaguid());
        summary.put("signatureCount", registration.getCredential() != null ? registration.getCredential().getSignatureCount() : null);
        return summary;
    }
}
