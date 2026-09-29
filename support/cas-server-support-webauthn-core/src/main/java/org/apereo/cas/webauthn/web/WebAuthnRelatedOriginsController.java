package org.apereo.cas.webauthn.web;

import module java.base;
import org.apereo.cas.web.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Publishes the origins that may use the CAS relying party identifier, as defined by
 * WebAuthn Level 3 related origin requests. Browsers fetch this document from
 * {@code https://<relying party id>/.well-known/webauthn}, outside any context path.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@ResponseBody
@Tag(name = "WebAuthN")
public class WebAuthnRelatedOriginsController extends AbstractController {
    /**
     * Related origins document endpoint.
     */
    public static final String ENDPOINT_RELATED_ORIGINS = "/.well-known/webauthn";

    private final Set<String> origins;

    @GetMapping(value = ENDPOINT_RELATED_ORIGINS, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Related origins that may use the relying party identifier")
    public ResponseEntity<Map<String, Set<String>>> relatedOrigins() {
        return ResponseEntity.ok(Map.of("origins", origins));
    }
}
