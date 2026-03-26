package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.web.AbstractRestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is {@link OidcCredentialIssuerMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Tag(name = "OpenID Connect")
public class OidcCredentialIssuerMetadataController extends AbstractRestController {

    private final OidcCredentialIssuerMetadataService metadataService;

    /**
     * Handle response entity.
     *
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL,
        "/**/" + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential issuer metadata request",
        description = "Handles requests for well-known OIDC credential issuer metadata")
    public ResponseEntity<OidcCredentialIssuerMetadata> handle() {
        val body = metadataService.build();
        return ResponseEntity.ok().body(body);
    }
}
