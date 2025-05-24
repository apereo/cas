package org.apereo.cas.oidc.federation;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is {@link OidcWellKnownFederationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag(name = "OpenID Connect")
public class OidcWellKnownFederationEndpointController extends BaseOidcController {
    private final OidcFederationEntityStatementService federationEntityStatementService;

    public OidcWellKnownFederationEndpointController(final OidcConfigurationContext configurationContext,
                                                     final OidcFederationEntityStatementService federationEntityStatementService) {
        super(configurationContext);
        this.federationEntityStatementService = federationEntityStatementService;
    }

    /**
     * Gets well known discovery federation configuration.
     *
     * @param request  the request
     * @param response the response
     * @return the well known discovery configuration
     */
    @GetMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL,
        "/**/" + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL
    })
    @Operation(summary = "Handle OIDC discovery federation request",
        description = "Handles requests for well-known OIDC discovery federation configuration")
    public ResponseEntity getWellKnownDiscoveryConfiguration(
        final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return ResponseEntity.badRequest()
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body);
        }

        val entityStatement = federationEntityStatementService.createAndSign();
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore().mustRevalidate())
            .header(HttpHeaders.ACCEPT, "application/jose;charset=UTF-8")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(entityStatement.getSignedStatement().serialize());
    }
}
