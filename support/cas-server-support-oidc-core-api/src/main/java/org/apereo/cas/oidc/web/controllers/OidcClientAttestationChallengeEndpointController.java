package org.apereo.cas.oidc.web.controllers;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.authn.OidcClientAttestationChallengeService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20DPoPNonceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Challenge endpoint of attestation-based client authentication (OAuth 2.0 Attestation-Based Client Authentication,
 * section 6.3), advertised as {@code challenge_endpoint} once challenges are turned on. A client fetches a challenge here
 * before building the proof of possession of its client attestation. The endpoint is public, and the answer is not cached.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcClientAttestationChallengeEndpointController extends BaseOidcController {
    private final OidcClientAttestationChallengeService challengeService;

    private final OAuth20DPoPNonceService dpopNonceService;

    public OidcClientAttestationChallengeEndpointController(final OidcConfigurationContext configurationContext,
                                                            final OidcClientAttestationChallengeService challengeService,
                                                            final OAuth20DPoPNonceService dpopNonceService) {
        super(configurationContext);
        this.challengeService = challengeService;
        this.dpopNonceService = dpopNonceService;
    }

    /**
     * Hand out a challenge and, once DPoP nonces are turned on, a DPoP nonce in the {@code DPoP-Nonce} header, for clients that
     * use the DPoP combined mode or DPoP on its own.
     *
     * @param request  the request
     * @param response the response
     * @return {@code attestation_challenge}, or {@code 404} when challenges are not turned on
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL,
        "/**/" + OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle client attestation challenge request",
        description = "Hands out a challenge for the proof of possession of a client attestation")
    public ResponseEntity handle(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        if (!isIssuerValidForEndpoint(request, response, List.of(OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer"));
        }
        if (!challengeService.isEnabled()) {
            return ResponseEntity.notFound().build();
        }
        if (dpopNonceService.isEnabled()) {
            dpopNonceService.provide(new JEEContext(request, response));
        }
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("attestation_challenge", challengeService.create()));
    }
}
