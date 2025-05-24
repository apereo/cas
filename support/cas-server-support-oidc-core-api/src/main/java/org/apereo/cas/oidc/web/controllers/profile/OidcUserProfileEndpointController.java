package org.apereo.cas.oidc.web.controllers.profile;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPIssuer;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPProtectedResourceRequestVerifier;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    protected void validateAccessToken(final String accessTokenId, final OAuth20AccessToken accessToken,
                                       final HttpServletRequest request, final HttpServletResponse response) {
        val dPopProof = request.getHeader(OAuth20Constants.DPOP);
        if (accessToken.getAuthentication().containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)) {
            val cnf = CollectionUtils.firstElement(accessToken.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_CONFIRMATION));
            cnf.ifPresent(Unchecked.consumer(conf -> {
                val confirmation = new JWKThumbprintConfirmation(new Base64URL(conf.toString()));
                val acceptedAlgs = getConfigurationContext().getDiscoverySettings().getDPopSigningAlgValuesSupported()
                    .stream()
                    .map(JWSAlgorithm::parse)
                    .collect(Collectors.toSet());

                val seconds = Beans.newDuration(getConfigurationContext().getCasProperties().getAuthn().getOidc().getCore().getSkew()).toSeconds();
                val verifier = new DPoPProtectedResourceRequestVerifier(acceptedAlgs, seconds, null);
                val signedProof = SignedJWT.parse(dPopProof);
                val dPoPIssuer = new DPoPIssuer(new ClientID(accessToken.getClientId()));
                Assert.notNull(JWTParser.parse(accessTokenId), "Provided access token id must be a (signed) JWT");
                val dpopAccessToken = new DPoPAccessToken(accessTokenId);
                verifier.verify(request.getMethod(), new URI(request.getRequestURL().toString()),
                    dPoPIssuer, signedProof, dpopAccessToken, confirmation, null);
            }));
        }
    }
}
