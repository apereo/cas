package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.profile.OidcUserProfileEndpointController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPProofException;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
class OidcAccessTokenEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAccessTokenController")
    protected OidcAccessTokenEndpointController oidcAccessTokenEndpointController;

    @Autowired
    @Qualifier("oidcProfileController")
    private OidcUserProfileEndpointController oidcProfileController;

    @Test
    void verifyBadEndpointRequest() throws Throwable {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        var mv = oidcAccessTokenEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.BAD_REQUEST, mv.getStatus());
        mv = oidcAccessTokenEndpointController.handleInvalidDPoPProofException(response, new InvalidDPoPProofException("invalid"));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.INVALID_DPOP_PROOF, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    void verifyClientNoCode() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.ACCESS_TOKEN_URL);
        val response = new MockHttpServletResponse();
        oidcAccessTokenEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        oidcAccessTokenEndpointController.handleGetRequest(request, response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    void verifyDPoPRequest() throws Throwable {
        val ecJWK = new ECKeyGenerator(Curve.P_256).keyID("1234567890").generate();
        val proofFactory = new DefaultDPoPProofFactory(ecJWK, JWSAlgorithm.ES256);

        var request = getHttpRequestForEndpoint("token");
        request.setMethod(HttpMethod.POST.name());
        var response = new MockHttpServletResponse();

        var uri = new URI(request.getRequestURL().toString());
        var dpopProof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), uri);
        var proofJwt = dpopProof.serialize();
        request.addHeader(OAuth20Constants.DPOP, proofJwt);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(oidcRegisteredService);
        request.addParameter(OAuth20Constants.CLIENT_ID, oidcRegisteredService.getClientId());
        val code = addCode(principal, oidcRegisteredService);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        request.addParameter(OAuth20Constants.CODE, code.getId());
        oauthInterceptor.preHandle(request, response, new Object());
        val mv = oidcAccessTokenEndpointController.handleRequest(request, response);
        val accessToken = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        assertNotNull(accessToken);
        val dpopAccessToken = JWTParser.parse(accessToken);
        assertNotNull(dpopAccessToken);

        request = getHttpRequestForEndpoint(OidcConstants.PROFILE_URL);
        request.setMethod(HttpMethod.POST.name());
        response = new MockHttpServletResponse();
        uri = new URI(request.getRequestURL().toString());
        dpopProof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), uri, new DPoPAccessToken(accessToken));
        val dpopJwt = dpopProof.serialize();
        request.addHeader(OAuth20Constants.DPOP, dpopJwt);
        request.addParameter(OAuth20Constants.TOKEN, accessToken);
        val entity = oidcProfileController.handlePostRequest(request, response);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

}
