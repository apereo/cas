package org.apereo.cas.webauthn.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.web.AbstractController;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Publishes the passkey endpoints document defined by the W3C "Well-Known URL for Relying Party Passkey Endpoints",
 * so that password managers and passkey providers can send users to the pages where passkeys are created and managed.
 * Clients fetch this document from {@code https://<relying party id>/.well-known/passkey-endpoints}, outside any
 * context path; an empty object still signals that CAS supports passkeys.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@ResponseBody
@Tag(name = "WebAuthN")
public class WebAuthnPasskeyEndpointsController extends AbstractController {
    /**
     * Passkey endpoints document endpoint.
     */
    public static final String ENDPOINT_PASSKEY_ENDPOINTS = "/.well-known/passkey-endpoints";

    private final CasConfigurationProperties casProperties;

    /**
     * Passkey endpoints document. Configured URLs are published as they are; a URL that is not configured
     * falls back to the account profile when account management is enabled,
     * and is left out otherwise.
     *
     * @return the response entity
     */
    @GetMapping(value = ENDPOINT_PASSKEY_ENDPOINTS, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Pages where passkeys for an account are created and managed")
    public ResponseEntity<Map<String, String>> passkeyEndpoints() {
        val core = casProperties.getAuthn().getMfa().getWebAuthn().getCore();
        val accountProfile = CasFeatureModule.FeatureCatalog.AccountManagement.isRegistered()
            ? casProperties.getServer().getPrefix() + '/' + CasWebflowConfigurer.FLOW_ID_ACCOUNT
            : null;
        val endpoints = new LinkedHashMap<String, String>();
        Optional.ofNullable(StringUtils.defaultIfBlank(core.getPasskeyEnrollUrl(), accountProfile))
            .ifPresent(url -> endpoints.put("enroll", url));
        Optional.ofNullable(StringUtils.defaultIfBlank(core.getPasskeyManageUrl(), accountProfile))
            .ifPresent(url -> endpoints.put("manage", url));
        return ResponseEntity.ok(endpoints);
    }
}
