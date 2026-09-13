package org.apereo.cas.oidc.web.controllers.profile;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcUserProfileEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag(name = "OpenID Connect")
public class OidcUserProfileEndpointController extends OAuth20UserProfileEndpointController<OidcConfigurationContext> {

    public OidcUserProfileEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL,
        "/**/" + OidcConstants.PROFILE_URL
    }, produces = {MediaType.APPLICATION_JSON_VALUE, OidcConstants.CONTENT_TYPE_JWT})
    @Operation(summary = "Handle user profile request",
        parameters = @Parameter(name = "access_token", in = ParameterIn.QUERY, required = true, description = "Access token"))
    @Override
    public ResponseEntity handleGetRequest(final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.PROFILE_URL, OAuth20Constants.PROFILE_URL))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        return super.handleGetRequest(request, response);
    }

    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL,
        "/**/" + OidcConstants.PROFILE_URL
    }, produces = {MediaType.APPLICATION_JSON_VALUE, OidcConstants.CONTENT_TYPE_JWT})
    @Operation(summary = "Handle user profile request",
        parameters = @Parameter(name = "access_token", in = ParameterIn.QUERY, required = true, description = "Access token"))
    @Override
    public ResponseEntity<String> handlePostRequest(final HttpServletRequest request,
                                                             final HttpServletResponse response) throws Exception {
        return handleGetRequest(request, response);
    }
}
