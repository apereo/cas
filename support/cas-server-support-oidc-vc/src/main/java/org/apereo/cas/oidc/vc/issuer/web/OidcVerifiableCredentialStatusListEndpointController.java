package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.status.OidcVerifiableCredentialStatusListService;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Publishes status list tokens (draft-ietf-oauth-status-list, section 8) that the {@code status} claim of issued
 * credentials references. The endpoint is public and allows cross-origin requests, so browser-based verifiers and
 * wallets can fetch it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag(name = "OpenID Connect")
public class OidcVerifiableCredentialStatusListEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    /**
     * Media type of status list tokens in JWT format.
     */
    public static final MediaType STATUS_LIST_JWT = MediaType.parseMediaType("application/statuslist+jwt");

    private final OidcVerifiableCredentialStatusListService statusListService;

    public OidcVerifiableCredentialStatusListEndpointController(final OidcConfigurationContext configurationContext,
                                                                final OidcVerifiableCredentialStatusListService statusListService) {
        super(configurationContext);
        this.statusListService = statusListService;
    }

    /**
     * Status list token of a status list.
     *
     * @param statusListId the status list identifier
     * @return the status list token, as the bare compact serialization, or {@code 404} when the status list is unknown or
     * has no unexpired entries
     * @throws Throwable the throwable
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_STATUS_LIST_URL + "/{statusListId}",
        "/**/" + OidcConstants.VC_STATUS_LIST_URL + "/{statusListId}"
    })
    @Operation(summary = "Get a status list token", description = "Returns the status list token of a status list")
    public ResponseEntity<byte[]> handle(@PathVariable("statusListId") final String statusListId) throws Throwable {
        val token = statusListService.buildStatusListToken(statusListId);
        return token
            .map(value -> ResponseEntity.ok()
                .contentType(STATUS_LIST_JWT)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .body(value.getBytes(StandardCharsets.US_ASCII)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
