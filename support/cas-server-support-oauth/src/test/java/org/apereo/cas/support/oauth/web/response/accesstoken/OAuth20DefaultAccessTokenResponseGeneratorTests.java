package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPIssuer;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPTokenRequestVerifier;
import com.nimbusds.oauth2.sdk.id.ClientID;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URI;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultAccessTokenResponseGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
class OAuth20DefaultAccessTokenResponseGeneratorTests extends AbstractOAuth20Tests {

    @Test
    void verifyAccessTokenAsDefault() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(false);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val model = mv.getModel();
        assertTrue(model.containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(model.containsKey(OAuth20Constants.EXPIRES_IN));
        assertFalse(model.containsKey(OAuth20Constants.SCOPE));
        assertTrue(model.containsKey(OAuth20Constants.TOKEN_TYPE));

        assertThrows(ParseException.class, () -> {
            val at = model.get(OAuth20Constants.ACCESS_TOKEN).toString();
            JWTParser.parse(at);
        });
    }

    @Test
    void verifyAccessTokenAsJwt() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }

    @Test
    void verifyRefreshTokenAsJwt() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), "secret", new LinkedHashSet<>());
        registeredService.setJwtRefreshToken(true);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }

    @Test
    void verifyDPoPAccessTokenAsJwt() throws Throwable {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);

        val registeredService = getRegisteredService("example", UUID.randomUUID().toString(), "secret");
        servicesManager.save(registeredService);

        val ecJWK = new ECKeyGenerator(Curve.P_256).keyID("1").generate();
        val proofFactory = new DefaultDPoPProofFactory(ecJWK, JWSAlgorithm.ES256);
        val signedProof = proofFactory.createDPoPJWT("POST", new URI(mockRequest.getRequestURL().toString()));

        val dPopIssuer = new DPoPIssuer(new ClientID(registeredService.getClientId()));
        val verifier = new DPoPTokenRequestVerifier(Set.of(JWSAlgorithm.ES256),
            new URI(mockRequest.getRequestURL().toString()), 30, null);
        
        val confirmation = verifier.verify(dPopIssuer, signedProof, null);
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser",
            Map.of(OAuth20Constants.DPOP, List.of(signedProof.serialize()),
                OAuth20Constants.DPOP_CONFIRMATION, List.of(confirmation.getValue().toString())));

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService,
            authentication, OAuth20GrantTypes.AUTHORIZATION_CODE, mockRequest);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.TOKEN_TYPE));
        assertEquals(OAuth20Constants.TOKEN_TYPE_DPOP, mv.getModel().get(OAuth20Constants.TOKEN_TYPE));
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }


    @Test
    void verifyAccessTokenAsJwtPerService() throws Throwable {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);

        val signingKey = new DefaultRegisteredServiceProperty();
        signingKey.addValue("pR3Vizkn5FSY5xCg84cIS4m-b6jomamZD68C8ash-TlNmgGPcoLgbgquxHPoi24tRmGpqHgM4mEykctcQzZ-Xg");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_KEY.getPropertyName(), signingKey);

        val encKey = new DefaultRegisteredServiceProperty();
        encKey.addValue("0KVXaN-nlXafRUwgsr3H_l6hkufY7lzoTy7OVI5pN0E");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY.getPropertyName(), encKey);

        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }

}
