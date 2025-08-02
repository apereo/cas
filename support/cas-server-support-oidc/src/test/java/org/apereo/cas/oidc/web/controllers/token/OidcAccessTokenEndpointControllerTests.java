package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.profile.OidcUserProfileEndpointController;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CertUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPProofException;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import lombok.val;
import org.apache.hc.core5.http.HttpHeaders;
import org.jose4j.keys.AesKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcAccessTokenEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDCWeb")
class OidcAccessTokenEndpointControllerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.accepted-issuers-pattern=.*")
    class MvcTests extends AbstractOidcTests {
        private MockMvc mvc;

        @BeforeEach
        void setup() {
            mvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .defaultRequest(post("/")
                    .contextPath("/cas")
                    .header("Host", "sso.example.org")
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .build();
        }

        @Test
        void verifyClientCredentialsWithPrivateJWT() throws Throwable {
            val key = EncodingUtils.generateJsonWebKey(512);
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setEncryptIdToken(false);
            registeredService.setJwks(key);
            registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.PRIVATE_KEY_JWT.getType());
            val claims = getClaims(registeredService.getClientId(),
                oidcIssuerService.determineIssuer(Optional.of(registeredService)),
                registeredService.getClientId(), registeredService.getClientId());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CLIENT_CREDENTIALS.getType()));
            servicesManager.save(registeredService);

            val jwt = new String(EncodingUtils.signJwsHMACSha512(new AesKey(key.getBytes(StandardCharsets.UTF_8)),
                claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of()), StandardCharsets.UTF_8);

            mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL)
                    .secure(true)
                    .queryParam(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .queryParam(OAuth20Constants.CLIENT_ASSERTION_TYPE, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER)
                    .queryParam(OAuth20Constants.CLIENT_ASSERTION, jwt)
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();
        }

        @Test
        void verifyClientCredentialsWithFormPost() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setEncryptIdToken(false);
            registeredService.setJwtAccessToken(true);
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CLIENT_CREDENTIALS.getType()));
            registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.CLIENT_SECRET_POST.getType());
            servicesManager.save(registeredService);
            mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                    .secure(true)
                    .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();
        }

        @Test
        void verifyClientCredentialsWithTlsEnabled() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setEncryptIdToken(false);
            registeredService.setJwtAccessToken(true);
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CLIENT_CREDENTIALS.getType()));
            registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH.getType());
            servicesManager.save(registeredService);
            val certificate = CertUtils.readCertificate(new ClassPathResource("RSA1024x509Cert.pem").getInputStream());
            mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                    .secure(true)
                    .queryParam(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name())
                    .requestAttr("jakarta.servlet.request.X509Certificate", new X509Certificate[]{certificate}))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();
        }

        @Test
        void verifyExchangeAccessTokenWithIdToken() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE.getType()));
            val tokenExchangePolicy = new DefaultRegisteredServiceOAuthTokenExchangePolicy()
                .setAllowedTokenTypes(Set.of(OAuth20TokenExchangeTypes.ID_TOKEN.getType()));
            registeredService.setTokenExchangePolicy(tokenExchangePolicy);
            servicesManager.save(registeredService);

            val accessToken = getAccessToken(registeredService.getClientId());
            ticketRegistry.addTicket(accessToken);

            val resource = "https://api.example.org/%s".formatted(UUID.randomUUID().toString());
            mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                    .secure(true)
                    .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                    .queryParam(OAuth20Constants.RESOURCE, resource)
                    .queryParam(OAuth20Constants.SUBJECT_TOKEN, accessToken.getId())
                    .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope()
                        + ' ' + OidcConstants.StandardScopes.EMAIL.getScope())
                    .queryParam(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType())
                    .queryParam(OAuth20Constants.REQUESTED_TOKEN_TYPE, OAuth20TokenExchangeTypes.ID_TOKEN.getType())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_token").exists())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();
        }

        @Test
        void verifyRefreshTokenUpdatesTicketGrantingTicket() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.REFRESH_TOKEN.getType(), OAuth20GrantTypes.AUTHORIZATION_CODE.getType()));
            registeredService.setGenerateRefreshToken(true);
            servicesManager.save(registeredService);

            val expirationPolicy = new TimeoutExpirationPolicy(2);
            val ticketGrantingTicket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                RegisteredServiceTestUtils.getAuthentication(), expirationPolicy);
            val expirationTime = expirationPolicy.getIdleExpirationTime(ticketGrantingTicket);
            val lastTimeUsed = ticketGrantingTicket.getLastTimeUsed();
            
            ticketRegistry.addTicket(ticketGrantingTicket);
            val code = addCode(ticketGrantingTicket, registeredService);
            val result = mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                    .secure(true)
                    .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                    .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                    .queryParam(OAuth20Constants.CODE, code.getId())
                    .queryParam(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_token").exists())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andReturn();

            val refreshToken = result.getModelAndView().getModel().get(OAuth20Constants.REFRESH_TOKEN).toString();
            val auth = EncodingUtils.encodeBase64(registeredService.getClientId() + ':' + registeredService.getClientSecret());

            for (var i = 0; i < 5; i++) {
                mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                        .secure(true)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                        .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                        .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                        .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                        .queryParam(OAuth20Constants.REFRESH_TOKEN, refreshToken)
                        .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id_token").exists())
                    .andExpect(jsonPath("$.access_token").exists());
                Thread.sleep(1000);
            }
            val tgt = ticketRegistry.getTicket(ticketGrantingTicket.getId(), TicketGrantingTicket.class);
            assertNotNull(tgt);
            val updatedExpirationTime = expirationPolicy.getIdleExpirationTime(tgt);
            val updatedLastTimeUsed = tgt.getLastTimeUsed();
            assertTrue(updatedExpirationTime.isAfter(expirationTime));
            assertTrue(updatedLastTimeUsed.isAfter(lastTimeUsed));
        }
    }

    @Nested
    class DefaultTests extends AbstractOidcTests {
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

}
