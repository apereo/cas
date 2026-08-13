package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcVerifiableCredentialTypeMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialTypeMetadataController extends BaseOAuth20Controller<OidcConfigurationContext> {

    private final OidcCredentialIssuerMetadataService metadataService;

    public OidcVerifiableCredentialTypeMetadataController(final OidcConfigurationContext configurationContext,
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
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + "/{configurationId}",
        "/**/" + OidcConstants.VC_CREDENTIAL_TYPE_URL + "/{configurationId}"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential configuration type request",
        description = "Handles requests for OIDC credential configuration type metadata",
        parameters = @Parameter(name = "configurationId", in = ParameterIn.PATH, description = "Configuration ID"))
    public ResponseEntity handle(final HttpServletRequest request, final HttpServletResponse response,
                                 @PathVariable final String configurationId) {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.VC_CREDENTIAL_TYPE_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return ResponseEntity.badRequest().body(body);
        }
        val body = metadataService.describeConfiguration(configurationId);
        return body == null
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok().body(body);
    }

}
