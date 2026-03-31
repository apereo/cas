package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcVerifiableCredentialIssuerMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialIssuerMetadataController extends BaseOAuth20Controller<OidcConfigurationContext> {

    private final OidcCredentialIssuerMetadataService metadataService;

    public OidcVerifiableCredentialIssuerMetadataController(final OidcConfigurationContext configurationContext,
                                                            final OidcCredentialIssuerMetadataService metadataService) {
        super(configurationContext);
        this.metadataService = metadataService;
    }

    /**
     * Handle response entity.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL,
        "/**/" + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential issuer metadata request",
        description = "Handles requests for well-known OIDC credential issuer metadata")
    public ResponseEntity handle(final HttpServletRequest request,
                                 final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return ResponseEntity.badRequest().body(body);
        }
        val body = metadataService.build();
        return ResponseEntity.ok().body(body);
    }
}
