package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.data.AuthenticatorTransport;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
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
     * Find the WebAuthn devices of a person. Credential ids, user handles and public keys are never returned.
     *
     * @param username the username
     * @return the registered devices, newest first
     */
    @McpTool(description = "Find the WebAuthn security keys and passkeys a person has registered, newest first: the "
        + "nickname, when it was registered, the transports it supports, whether it is a discoverable credential (passkey), "
        + "the AAGUID that identifies the authenticator model and its signature count. Credential ids and key material "
        + "are never returned.",
        title = "Find WebAuthn Devices",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public WebAuthnDevices findWebAuthnDevices(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val repository = webAuthnCredentialRepository.getIfAvailable();
        if (repository == null) {
            return new WebAuthnDevices(username, 0, List.of(), "WebAuthn multifactor authentication is not configured");
        }
        val devices = repository.getRegistrationsByUsername(username)
            .stream()
            .sorted(Comparator.comparing(CredentialRegistration::getRegistrationTime,
                Comparator.nullsLast(Comparator.<Instant>reverseOrder())))
            .map(CasWebAuthnTools::summarize)
            .toList();
        return new WebAuthnDevices(username, devices.size(), devices, null);
    }

    private static WebAuthnDevice summarize(final CredentialRegistration registration) {
        val transports = registration.getTransports();
        return new WebAuthnDevice(registration.getCredentialNickname(), String.valueOf(registration.getRegistrationTime()),
            transports != null ? transports.stream().map(AuthenticatorTransport::getId).toList() : List.of(),
            registration.getDiscoverable(), registration.getAaguid(),
            registration.getCredential() != null ? registration.getCredential().getSignatureCount() : null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record WebAuthnDevices(String username, int count, List<WebAuthnDevice> devices, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record WebAuthnDevice(@Nullable String nickname, String registrationTime, List<String> transports,
                                 @Nullable Boolean discoverable, @Nullable String aaguid, @Nullable Long signatureCount) {
    }
}
