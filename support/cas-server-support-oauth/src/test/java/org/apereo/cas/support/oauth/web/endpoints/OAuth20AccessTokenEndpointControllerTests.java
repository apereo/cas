package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("OAuthWeb")
class OAuth20AccessTokenEndpointControllerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.discovery.scopes=openid,profile,create,email,read,update")
    class MvcTests extends AbstractOAuth20Tests {
        private MockMvc mvc;

        @BeforeEach
        void setup() {
            mvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .defaultRequest(get("/")
                    .contextPath("/cas")
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                    .contentType(MediaType.APPLICATION_JSON))
                .build();
        }

        @Test
        void verifyClientCredentialsWithTlsEnabled() throws Throwable {
            val service = getRegisteredService(OAuth20GrantTypes.CLIENT_CREDENTIALS);
            service.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH.getType());
            servicesManager.save(service);
            val certificate = CertUtils.readCertificate(new ClassPathResource("RSA1024x509Cert.pem").getInputStream());
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .queryParam(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name())
                    .requestAttr("jakarta.servlet.request.X509Certificate", new X509Certificate[]{certificate}))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();
        }

        @Test
        void verifyClientCredentialsWithTlsUnknown() throws Throwable {
            val service = getRegisteredService(OAuth20GrantTypes.CLIENT_CREDENTIALS);
            service.setTokenEndpointAuthenticationMethod("unknown");
            servicesManager.save(service);
            val certificate = CertUtils.readCertificate(new ClassPathResource("RSA1024x509Cert.pem").getInputStream());
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .queryParam(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name())
                    .requestAttr("jakarta.servlet.request.X509Certificate", new X509Certificate[]{certificate}))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.access_token").doesNotExist())
                .andReturn();
        }

        @Test
        void verifyX509ClientCredentialsFailsToPassIP() throws Throwable {
            val service = getRegisteredService(OAuth20GrantTypes.CLIENT_CREDENTIALS);
            service.setJwtAccessToken(true);
            service.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH.getType());
            service.setTlsClientAuthSanIp("1.2.3.4");
            servicesManager.save(service);
            val certificate = CertUtils.readCertificate(new ClassPathResource("RSA1024x509Cert.pem").getInputStream());
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .queryParam(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name())
                    .requestAttr("jakarta.servlet.request.X509Certificate", new X509Certificate[]{certificate}))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.access_token").doesNotExist());
        }

        @Test
        void verifyX509ClientCredentialsAsJWT() throws Throwable {
            val service = getRegisteredService(OAuth20GrantTypes.CLIENT_CREDENTIALS);
            service.setJwtAccessToken(true);
            service.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH.getType());
            servicesManager.save(service);
            val certificate = CertUtils.readCertificate(new ClassPathResource("RSA1024x509Cert.pem").getInputStream());
            val accessToken = mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .queryParam(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name())
                    .requestAttr("jakarta.servlet.request.X509Certificate", new X509Certificate[]{certificate}))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(request().attribute(OAuth20Constants.REQUEST_ATTRIBUTE_ACCESS_TOKEN_REQUEST, Boolean.TRUE))
                .andReturn()
                .getModelAndView()
                .getModel()
                .get("access_token");

            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.INTROSPECTION_URL)
                    .param(OAuth20Constants.TOKEN, accessToken.toString())
                    .headers(HttpUtils.createBasicAuthHeaders(service.getClientId(), service.getClientSecret()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnf.x5t#S256").exists())
                .andReturn();

        }

        @Test
        void verifyClientCredentialsWithBasicAuth() throws Throwable {
            val service = getRegisteredService(OAuth20GrantTypes.CLIENT_CREDENTIALS);
            service.setJwtAccessToken(true);
            service.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.CLIENT_SECRET_BASIC.getType());
            servicesManager.save(service);
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .headers(HttpUtils.createBasicAuthHeaders(service.getClientId(), service.getClientSecret()))
                    .queryParam(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();
        }

        @Test
        void verifyClientCredentialsWithFormPost() throws Throwable {
            val service = getRegisteredService(OAuth20GrantTypes.CLIENT_CREDENTIALS);
            service.setJwtAccessToken(true);
            service.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.CLIENT_SECRET_POST.getType());
            servicesManager.save(service);
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .param(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecret())
                    .queryParam(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();
        }

        @Test
        void verifyClientCredentialsUnauthorized() throws Throwable {
            val service = getRegisteredService(OAuth20GrantTypes.CLIENT_CREDENTIALS);
            service.setJwtAccessToken(true);
            service.setTokenEndpointAuthenticationMethod("authn_method_invalid");
            servicesManager.save(service);
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .param(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecret())
                    .queryParam(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.name()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.access_token").doesNotExist())
                .andReturn();
        }

        @Test
        void verifyAccessTokenToAccessTokenExchange() throws Throwable {
            val service = getRegisteredService(UUID.randomUUID().toString(), OAuth20GrantTypes.TOKEN_EXCHANGE);
            val subjectToken = getAccessToken(service.getServiceId(), service.getClientId());
            service.setScopes(Set.of("create", "update"));
            servicesManager.save(service);
            ticketRegistry.addTicket(subjectToken);

            val resource = "https://api.example.org/%s".formatted(UUID.randomUUID().toString());
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .param(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecret())
                    .queryParam(OAuth20Constants.RESOURCE, resource)
                    .queryParam(OAuth20Constants.SUBJECT_TOKEN, subjectToken.getId())
                    .queryParam(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType())
                    .queryParam(OAuth20Constants.REQUESTED_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType())
                    .queryParam(OAuth20Constants.SCOPE, "update")
                )
                .andExpect(jsonPath("$.scope").value("update"))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.issued_token_type").value(OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType()));
        }

        @Test
        void verifyAccessTokenToJWTExchange() throws Throwable {
            val service = getRegisteredService(UUID.randomUUID().toString(), OAuth20GrantTypes.TOKEN_EXCHANGE);
            val subjectToken = getAccessToken(service.getServiceId(), service.getClientId());
            service.setScopes(Set.of("create", "update"));
            servicesManager.save(service);
            ticketRegistry.addTicket(subjectToken);

            val resource = "https://api.example.org/%s".formatted(UUID.randomUUID().toString());
            mvc.perform(post("/cas" + CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL)
                    .param(OAuth20Constants.CLIENT_ID, service.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecret())
                    .queryParam(OAuth20Constants.RESOURCE, resource)
                    .queryParam(OAuth20Constants.SUBJECT_TOKEN, subjectToken.getId())
                    .queryParam(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType())
                    .queryParam(OAuth20Constants.REQUESTED_TOKEN_TYPE, OAuth20TokenExchangeTypes.JWT.getType())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType())
                    .queryParam(OAuth20Constants.SCOPE, "update")
                )
                .andExpect(jsonPath("$.scope").value("update"))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.issued_token_type").value(OAuth20TokenExchangeTypes.JWT.getType()));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.ticket.track-descendant-tickets=false")
    class DefaultTests extends AbstractOAuth20Tests {
        /**
         * Check the registered services always contain empty allowed grant types.
         * These tests are run to ensure that
         * the change that adds proper support for supported grant types does not break existing CAS
         * setups that does not specify allowed grant types. Briefly, it checks that empty  supported grant types
         * is equivalent to supported grant types with all valid values.
         *
         * @return stream of services for tests
         */
        public static Stream<OAuthRegisteredService> getParameters() {
            return Stream.of(
                getRegisteredService(REDIRECT_URI, CLIENT_SECRET, CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE)),
                getRegisteredService(REDIRECT_URI, CLIENT_SECRET, EnumSet.noneOf(OAuth20GrantTypes.class))
            );
        }

        @BeforeEach
        void initialize() {
            ticketRegistry.deleteAll();
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientNoClientId(final OAuthRegisteredService registeredService) throws Throwable {
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            val principal = createPrincipal();
            servicesManager.save(registeredService);
            val code = addCode(principal, registeredService);
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get("error"));
        }

        @Test
        void verifyClientNoRedirectUri() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
            val code = addCode(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get("error"));
        }


        @Test
        void verifyClientNoAuthorizationCode() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
            val code = addCode(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
        }

        @Test
        void verifyClientBadGrantType() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
            val code = addCode(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, "badValue");
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
        }

        @Test
        void verifyClientDisallowedGrantType() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
            val code = addCode(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());

            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get("error"));
        }

        @Test
        void verifyClientNoClientSecret() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
            val code = addCode(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get("error"));
        }

        @Test
        void verifyClientNoCode() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));

            addCode(principal, service);

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientNoCasService(final OAuthRegisteredService registeredService) throws Throwable {
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            val principal = createPrincipal();
            val code = addCode(principal, registeredService);
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientRedirectUriDoesNotStartWithServiceId(final OAuthRegisteredService registeredService) throws Throwable {
            val principal = createPrincipal();
            servicesManager.save(registeredService);
            val code = addCode(principal, registeredService);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, OTHER_REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));

            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientWrongSecret(final OAuthRegisteredService registeredService) throws Throwable {
            val principal = createPrincipal();
            servicesManager.save(registeredService);
            val code = addCode(principal, registeredService);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyClientEmptySecret() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE), StringUtils.EMPTY);
            val code = addCode(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, StringUtils.EMPTY);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        }

        @Test
        void verifyPKCECodeVerifier() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE), CLIENT_SECRET);
            val code = addCodeWithChallenge(principal, service, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, CODE_CHALLENGE);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));

            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyPKCEInvalidCodeVerifier(final OAuthRegisteredService registeredService) throws Throwable {
            val principal = createPrincipal();
            servicesManager.save(registeredService);
            val code = addCodeWithChallenge(principal, registeredService, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, "invalidcode");
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));

            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyPKCEEmptySecret() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE), StringUtils.EMPTY);
            val code = addCodeWithChallenge(principal, service, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, StringUtils.EMPTY);
            mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, CODE_CHALLENGE);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));

            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyPKCEWrongSecret(final OAuthRegisteredService registeredService) throws Throwable {
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, CODE_CHALLENGE);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            val principal = createPrincipal();
            servicesManager.save(registeredService);
            val code = addCodeWithChallenge(principal, registeredService, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientExpiredCode(final OAuthRegisteredService registeredService) throws Throwable {
            servicesManager.save(registeredService);

            val map = new HashMap<String, List<Object>>();
            map.put(NAME, List.of(VALUE));
            val list = List.of(VALUE, VALUE);
            map.put(NAME2, (List) list);

            val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
            val authentication = getAuthentication(principal);
            val expiringOAuthCodeFactory = new OAuth20DefaultOAuthCodeFactory(new DefaultUniqueTicketIdGenerator(),
                alwaysExpiresExpirationPolicyBuilder(), servicesManager, CipherExecutor.noOpOfStringToString(),
                descendantTicketsTrackingPolicy);
            val service = serviceFactory.createService(registeredService.getServiceId());
            val code = expiringOAuthCodeFactory.create(service, authentication,
                new MockTicketGrantingTicket("casuser"), new ArrayList<>(), null,
                null, registeredService.getClientId(), new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
            ticketRegistry.addTicket(code);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientAuthByParameter(final OAuthRegisteredService registeredService) throws Throwable {
            servicesManager.save(registeredService);
            assertClientOK(registeredService, false);
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientAuthWithJwtAccessToken(final OAuthRegisteredService registeredService) throws Throwable {
            registeredService.setJwtAccessToken(true);
            servicesManager.save(registeredService);
            assertClientOK(registeredService, false);
        }

        @Test
        void verifyDeviceFlowGeneratesCode() throws Throwable {
            val service = addRegisteredService();
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
            val mockResponse = new MockHttpServletResponse();

            val commonProfile = new CommonProfile();
            commonProfile.setId("testuser");
            new ProfileManager(new JEEContext(mockRequest, mockResponse), oauthDistributedSessionStore)
                .save(true, commonProfile, false);

            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            val model = mv.getModel();
            assertTrue(model.containsKey(OAuth20Constants.DEVICE_CODE));
            assertTrue(model.containsKey(OAuth20Constants.DEVICE_VERIFICATION_URI));
            assertTrue(model.containsKey(OAuth20Constants.DEVICE_USER_CODE));
            assertTrue(model.containsKey(OAuth20Constants.DEVICE_INTERVAL));
            assertTrue(model.containsKey(OAuth20Constants.EXPIRES_IN));

            val devCode = model.get(OAuth20Constants.DEVICE_CODE).toString();
            val userCode = model.get(OAuth20Constants.DEVICE_USER_CODE).toString();

            val devReq = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.DEVICE_AUTHZ_URL);
            devReq.setParameter(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE, userCode);
            val devResp = new MockHttpServletResponse();
            val mvDev = deviceController.handlePostRequest(devReq, devResp);
            assertNotNull(mvDev);
            val status = mvDev.getStatus();
            assertNotNull(status);
            assertTrue(status.is2xxSuccessful());

            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
            mockRequest.setParameter(OAuth20Constants.DEVICE_CODE, devCode);
            val approveResp = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, approveResp, null);
            val mvApproved = accessTokenController.handleGetRequest(mockRequest, approveResp);
            assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
            assertEquals(getDefaultAccessTokenExpiration(), mvApproved.getModel().get(OAuth20Constants.EXPIRES_IN));
            assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.TOKEN_TYPE));
        }

        @ParameterizedTest
        @MethodSource("getParameters")
        void verifyClientAuthByHeader(final OAuthRegisteredService registeredService) throws Throwable {
            servicesManager.save(registeredService);
            assertClientOK(registeredService, false);
        }

        @Test
        void verifyClientAuthByParameterWithRefreshToken() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.AUTHORIZATION_CODE), randomServiceUrl());
            service.setGenerateRefreshToken(true);
            assertClientOK(service, true);
        }

        @Test
        void verifyClientAuthByHeaderWithRefreshToken() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.AUTHORIZATION_CODE), randomServiceUrl());
            service.setGenerateRefreshToken(true);
            assertClientOK(service, true);
        }

        @Test
        void verifyClientAuthJsonByParameterWithRefreshToken() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.AUTHORIZATION_CODE), randomServiceUrl());
            service.setGenerateRefreshToken(true);
            assertClientOK(service, true);
        }

        @Test
        void verifyClientAuthJsonByHeaderWithRefreshToken() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.AUTHORIZATION_CODE), randomServiceUrl());
            service.setGenerateRefreshToken(true);
            assertClientOK(service, true);
        }

        @Test
        void ensureOnlyRefreshTokenIsAcceptedForRefreshGrant() throws Throwable {
            val service = addRegisteredService(true, CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD, OAuth20GrantTypes.REFRESH_TOKEN));
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            val mockSession = new MockHttpSession();
            mockRequest.setSession(mockSession);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(USERNAME, GOOD_USERNAME);
            mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);

            var mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            var mv = accessTokenController.handleRequest(mockRequest, mockResponse);

            assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
            val refreshToken = mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString();
            val accessToken = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, accessToken);

            mockResponse = new MockHttpServletResponse();
            accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());

            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken);
            mockResponse = new MockHttpServletResponse();
            mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        }

        @Test
        void verifyUserNoClientId() throws Throwable {
            addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(USERNAME, GOOD_USERNAME);
            mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyUserNoCasService() throws Throwable {
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, "unknown-client-id");
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(USERNAME, GOOD_USERNAME);
            mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyUserBadAuthorizationCode() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.AUTHORIZATION_CODE), UUID.randomUUID().toString(), randomServiceUrl());
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, service.getClientSecret());
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(USERNAME, GOOD_USERNAME);
            mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyUserBadCredentials() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.PASSWORD), UUID.randomUUID().toString(), randomServiceUrl());
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(USERNAME, GOOD_USERNAME);
            mockRequest.setParameter(PASSWORD, "badPassword");
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyUserAuth() throws Throwable {
            val registeredService = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
            assertUserAuth(false, true, registeredService);
        }

        @Test
        void verifyUserAuthForServiceWithoutSecret() throws Throwable {
            val registeredService = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD), StringUtils.EMPTY);
            assertUserAuth(false, false, registeredService);
        }

        @Test
        void verifyUserAuthWithRefreshToken() throws Throwable {
            val registeredService = addRegisteredService(Set.of(OAuth20GrantTypes.PASSWORD), UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setGenerateRefreshToken(true);
            assertUserAuth(true, true, registeredService);
        }

        @Test
        void verifyJsonUserAuth() throws Throwable {
            val registeredService = addRegisteredService(Set.of(OAuth20GrantTypes.PASSWORD), UUID.randomUUID().toString(), randomServiceUrl());
            assertUserAuth(false, true, registeredService);
        }

        @Test
        void verifyJsonUserAuthWithRefreshToken() throws Throwable {
            val registeredService = addRegisteredService(Set.of(OAuth20GrantTypes.PASSWORD), UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setGenerateRefreshToken(true);
            assertUserAuth(true, true, registeredService);
        }

        @Test
        void verifyRefreshTokenExpiredToken() throws Throwable {
            val principal = createPrincipal();
            val registeredService = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN), UUID.randomUUID().toString(),
                randomServiceUrl());
            val authentication = getAuthentication(principal);
            val service = serviceFactory.createService(registeredService.getServiceId());
            val expiringRefreshTokenFactory = new OAuth20DefaultRefreshTokenFactory(
                alwaysExpiresExpirationPolicyBuilder(), ticketRegistry,
                servicesManager, descendantTicketsTrackingPolicy, casProperties);
            val refreshToken = expiringRefreshTokenFactory.create(service, authentication,
                new MockTicketGrantingTicket("casuser"), new ArrayList<>(), registeredService.getClientId(), StringUtils.EMPTY, new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
            ticketRegistry.addTicket(refreshToken);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyRefreshTokenBadCredentials() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN), UUID.randomUUID().toString(), randomServiceUrl());
            val refreshToken = addRefreshToken(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyRefreshTokenEmptySecret() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN), UUID.randomUUID().toString(), StringUtils.EMPTY, randomServiceUrl());
            val refreshToken = addRefreshToken(principal, service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, StringUtils.EMPTY);
            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        }

        @Test
        void verifyRefreshTokenMissingToken() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN), UUID.randomUUID().toString(), randomServiceUrl());

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, service.getClientSecret());
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_GRANT, mv.getModel().get(OAuth20Constants.ERROR));
        }

        @Test
        void verifyRefreshTokenOKWithExpiredTicketGrantingTicket() throws Throwable {
            val principal = createPrincipal();
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN), UUID.randomUUID().toString(), randomServiceUrl());
            val refreshToken = addRefreshToken(principal, service);

            refreshToken.getTicketGrantingTicket().markTicketExpired();
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, service.getClientSecret());
            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());

            val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

            val accessToken = ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
            assertEquals(principal, accessToken.getAuthentication().getPrincipal());

            val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
            assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
        }

        @Test
        void verifyRefreshTokenOK() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN),
                UUID.randomUUID().toString(), randomServiceUrl());
            assertRefreshTokenOk(service);
        }

        @Test
        void verifyRefreshTokenWithNoAuthentication() throws Throwable {
            val registeredService = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN),
                UUID.randomUUID().toString(), randomServiceUrl());
            val principal = createPrincipal();
            val service = serviceFactory.createService(registeredService.getServiceId());
            val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(ticketGrantingTicket);
            val refreshToken = defaultRefreshTokenFactory.create(service,
                ticketGrantingTicket, registeredService.getClientId(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
            ticketRegistry.addTicket(refreshToken);
            assertRefreshTokenOk(registeredService, refreshToken, principal);
        }

        @Test
        void verifyRefreshTokenOKWithRefreshToken() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN),
                UUID.randomUUID().toString(), randomServiceUrl());
            service.setGenerateRefreshToken(true);
            service.setRenewRefreshToken(true);
            assertRefreshTokenOk(service);
        }

        @Test
        void verifyJsonRefreshTokenOK() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN),
                UUID.randomUUID().toString(), randomServiceUrl());
            assertRefreshTokenOk(service);
        }

        @Test
        void verifyJsonRefreshTokenOKWithRefreshToken() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN),
                UUID.randomUUID().toString(), randomServiceUrl());
            service.setGenerateRefreshToken(true);
            service.setRenewRefreshToken(true);
            assertRefreshTokenOk(service);
        }

        @Test
        void verifyAccessTokenRequestWithRefreshTokenCannotExceedScopes() throws Throwable {
            val service = addRegisteredService(Set.of(OAuth20GrantTypes.REFRESH_TOKEN), UUID.randomUUID().toString(), randomServiceUrl());
            val principal = createPrincipal();
            val refreshToken = addRefreshTokenWithScope(principal, List.of("profile"), service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, service.getClientSecret());
            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
            mockRequest.setParameter(OAuth20Constants.SCOPE, "email");

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);

            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
            assertEquals(OAuth20Constants.INVALID_SCOPE, mv.getModel().get(OAuth20Constants.ERROR).toString());
        }

        @Test
        void verifyAccessTokenRequestWithRefreshTokenWithoutRequestingScopes() throws Throwable {
            val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
            val principal = createPrincipal();
            val refreshToken = addRefreshTokenWithScope(principal, List.of("profile"), service);

            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
            mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);

            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

            if (service.isRenewRefreshToken()) {
                assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
            } else {
                assertFalse(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
            }
            val newRefreshToken = service.isRenewRefreshToken()
                ? ticketRegistry.getTicket(mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString(), OAuth20RefreshToken.class)
                : refreshToken;
            assertNotNull(newRefreshToken);
            assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
            val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

            val accessToken = ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
            assertEquals(principal, accessToken.getAuthentication().getPrincipal());

            val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
            assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
        }

        private void assertUserAuth(final boolean refreshToken, final boolean withClientSecret,
                                    final OAuthRegisteredService registeredService) throws Throwable {
            val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
            mockRequest.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            if (withClientSecret) {
                mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
            }
            mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase(Locale.ENGLISH));
            mockRequest.setParameter(USERNAME, GOOD_USERNAME);
            mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
            mockRequest.addHeader(CasProtocolConstants.PARAMETER_SERVICE, REDIRECT_URI);
            val mockResponse = new MockHttpServletResponse();
            requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
            val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
            assertEquals(200, mockResponse.getStatus());
            assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
            if (refreshToken) {
                assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
            }
            assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));

            val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

            val accessToken = ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
            assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

            val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
            assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
        }

        private OAuth20RefreshToken addRefreshTokenWithScope(
            final Principal principal, final List<String> scopes,
            final OAuthRegisteredService registeredService) throws Throwable {
            val authentication = getAuthentication(principal);
            val service = serviceFactory.createService(registeredService.getServiceId());
            val refreshToken = defaultRefreshTokenFactory.create(service, authentication,
                new MockTicketGrantingTicket("casuser"),
                scopes, registeredService.getClientId(), StringUtils.EMPTY, new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
            ticketRegistry.addTicket(refreshToken);
            return refreshToken;
        }
    }
}
