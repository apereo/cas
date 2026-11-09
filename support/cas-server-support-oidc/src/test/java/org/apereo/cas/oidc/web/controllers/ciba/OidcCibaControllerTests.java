package org.apereo.cas.oidc.web.controllers.ciba;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcCibaRequestCompactor;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.registry.StatelessTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.DirectObjectProvider;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcCibaControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",

    "cas.http-client.host-name-verifier=none",

    "cas.authn.attribute-repository.stub.attributes.email=casuser@apereo.org",
    "cas.authn.attribute-repository.stub.attributes.name=CAS",

    "cas.authn.oidc.ciba.verification.delay=PT1S",
    "cas.authn.oidc.ciba.verification.mail.html=false",
    "cas.authn.oidc.ciba.verification.mail.from=cas@apereo.org",
    "cas.authn.oidc.ciba.verification.mail.subject=CIBA Token",
    "cas.authn.oidc.ciba.verification.mail.text=URL is ${verificationUrl}"
})
@EnabledIfListeningOnPort(port = 25000)
class OidcCibaControllerTests extends AbstractOidcTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(TicketCatalog.BEAN_NAME)
    private TicketCatalog ticketCatalog;

    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    private TicketSerializationManager ticketSerializationManager;

    @Test
    void verifyRequestWithoutScopes() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithMultipleHints() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.LOGIN_HINT_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    /**
     * A signed authentication request (CIBA Core 1.0, section 7.1.1) for another client, or one signed with a client
     * secret, is refused rather than set aside for the parameters of the request, which alone are accepted.
     */
    @Test
    void verifyInvalidSignedAuthenticationRequestIsRefused() throws Throwable {
        try (val webServer = new MockWebServer(true)) {
            webServer.start();
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            registeredService.setBackchannelClientNotificationEndpoint("https://localhost:%s".formatted(webServer.getPort()));
            registeredService.setClientSecrets(new ArrayList<>());
            registeredService.setClientSecret(RandomUtils.randomAlphanumeric(64));
            servicesManager.save(registeredService);

            mockMvc.perform(cibaRequest(registeredService, null)).andExpect(status().isOk());

            val issuer = oidcIssuerService.determineIssuer(Optional.of(registeredService));
            val forAnotherClient = signWithClientSecret(registeredService, new JWTClaimsSet.Builder()
                .claim(OAuth20Constants.CLIENT_ID, UUID.randomUUID().toString()));
            mockMvc.perform(cibaRequest(registeredService, forAnotherClient))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                .andExpect(jsonPath("$.error_description").value(Matchers.startsWith("Request object cannot be verified")));

            val signedWithSecret = signWithClientSecret(registeredService, new JWTClaimsSet.Builder()
                .issuer(registeredService.getClientId())
                .audience(issuer)
                .issueTime(new Date())
                .notBeforeTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .jwtID(UUID.randomUUID().toString()));
            mockMvc.perform(cibaRequest(registeredService, signedWithSecret))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_description").value("Request object must be signed with an asymmetric algorithm"));
        }
    }

    /**
     * A signed authentication request is verified with the keys of the authenticated client, whether or not the request
     * names it with {@code client_id}; it must carry {@code iss}, {@code aud}, {@code exp}, {@code iat}, {@code nbf} and
     * {@code jti}, be valid for 60 minutes at most, not be issued in the future, and is used once. A request
     * authenticated as one client that names another registered client is refused, and so is any signed authentication
     * request when CAS does not support the request parameter.
     */
    @Test
    void verifySignedAuthenticationRequest() throws Throwable {
        try (val webServer = new MockWebServer(true)) {
            webServer.start();
            val signingKey = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            registeredService.setBackchannelClientNotificationEndpoint("https://localhost:%s".formatted(webServer.getPort()));
            registeredService.setClientSecrets(new ArrayList<>());
            registeredService.setClientSecret(RandomUtils.randomAlphanumeric(64));
            registeredService.setJwks(new JWKSet(signingKey.toPublicJWK()).toString());
            servicesManager.save(registeredService);

            val issuer = oidcIssuerService.determineIssuer(Optional.of(registeredService));
            val now = Instant.now(Clock.systemUTC());
            val claims = (Supplier<JWTClaimsSet.Builder>) () -> new JWTClaimsSet.Builder()
                .issuer(registeredService.getClientId())
                .audience(issuer)
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .jwtID(UUID.randomUUID().toString());
            val requestObject = signWithKey(signingKey, claims.get());
            mockMvc.perform(cibaRequest(registeredService, requestObject, false)).andExpect(status().isOk());
            mockMvc.perform(cibaRequest(registeredService, requestObject, false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_description").value("Request object jti has already been used"));

            val invalid = List.of(
                Map.entry("Request object must be issued by client " + registeredService.getClientId(), claims.get().issuer("https://other.example.org")),
                Map.entry("Request object must be addressed to " + issuer, claims.get().audience("https://other.example.org")),
                Map.entry("Request object must carry iat and jti", claims.get().jwtID(null)),
                Map.entry("Request object has no expiration or has expired", claims.get().expirationTime(null)),
                Map.entry("Request object has no expiration or has expired", claims.get().expirationTime(Date.from(now.minus(Duration.ofHours(1))))),
                Map.entry("Request object has no nbf or is not valid yet", claims.get().notBeforeTime(null)),
                Map.entry("Request object has no nbf or is not valid yet", claims.get().notBeforeTime(Date.from(now.plus(Duration.ofHours(1))))),
                Map.entry("Request object is issued in the future", claims.get().issueTime(Date.from(now.plus(Duration.ofHours(1))))),
                Map.entry("Request object may be valid for 60 minutes at most", claims.get().expirationTime(Date.from(now.plus(Duration.ofHours(2))))));
            for (val entry : invalid) {
                mockMvc.perform(cibaRequest(registeredService, signWithKey(signingKey, entry.getValue()), true))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_description").value(entry.getKey()));
            }
            val otherClient = getOidcRegisteredService(UUID.randomUUID().toString());
            otherClient.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            otherClient.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
            otherClient.setBackchannelClientNotificationEndpoint(registeredService.getBackchannelClientNotificationEndpoint());
            otherClient.setJwks(registeredService.getJwks());
            servicesManager.save(otherClient);
            mockMvc.perform(cibaRequest(registeredService, signWithKey(signingKey, claims.get()), false)
                    .queryParam(OAuth20Constants.CLIENT_ID, otherClient.getClientId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_description").value("Request object cannot be verified or is not for client " + registeredService.getClientId()));

            val properties = new CasConfigurationProperties();
            properties.getAuthn().getOidc().getDiscovery().setRequestParameterSupported(false);
            val context = spy(oidcConfigurationContext);
            when(context.getCasProperties()).thenReturn(properties);
            val request = new MockHttpServletRequest();
            request.setParameter(OAuth20Constants.REQUEST, signWithKey(signingKey, claims.get()));
            assertEquals("Signed authentication requests are not supported",
                new OidcCibaController(context, List.of()).validateRequestObject(request, registeredService));
        }
    }

    private MockHttpServletRequestBuilder cibaRequest(final OidcRegisteredService registeredService, @Nullable final String requestObject) {
        return cibaRequest(registeredService, requestObject, true);
    }

    private MockHttpServletRequestBuilder cibaRequest(final OidcRegisteredService registeredService, @Nullable final String requestObject,
                                                      final boolean withClientId) {
        val request = post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
            .secure(true)
            .with(withHttpRequestProcessor())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
            .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
            .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
            .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
            .queryParam(OidcConstants.LOGIN_HINT, "casuser");
        if (withClientId) {
            request.queryParam(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        }
        return requestObject == null ? request : request.queryParam(OAuth20Constants.REQUEST, requestObject);
    }

    private static String signWithKey(final RSAKey signingKey, final JWTClaimsSet.Builder claims) throws Exception {
        val requestObject = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(), claims
            .claim(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
            .claim(OidcConstants.LOGIN_HINT, "casuser")
            .build());
        requestObject.sign(new RSASSASigner(signingKey));
        return requestObject.serialize();
    }

    private static String signWithClientSecret(final OidcRegisteredService registeredService, final JWTClaimsSet.Builder claims) throws Exception {
        val requestObject = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims
            .claim(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
            .claim(OidcConstants.LOGIN_HINT, "casuser")
            .build());
        requestObject.sign(new MACSigner(registeredService.getClientSecret().getBytes(StandardCharsets.UTF_8)));
        return requestObject.serialize();
    }

    @Test
    void verifyRequestWithInvalidUserCode() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.USER_CODE, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());

        registeredService.setBackchannelUserCodeParameterSupported(true);
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OAuth20Constants.RESOURCE, "https://api.example.org/")
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_TARGET));
    }

    @Test
    void verifyRequestWithMissingClientToken() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithBadNotificationEndpoint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        registeredService.setBackchannelClientNotificationEndpoint("http://ciba.example.org");

        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithIdTokenHint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        registeredService.setBackchannelClientNotificationEndpoint("https://ciba.example.org");
        registeredService.setGenerateRefreshToken(true);
        servicesManager.save(registeredService);

        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());

        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(getAccessToken(registeredService.getClientId()))
            .userProfile(profile)
            .responseType(OAuth20ResponseTypes.CODE)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .registeredService(registeredService)
            .build();
        val idTokenHint = oidcIdTokenGenerator.generate(idTokenContext).token();

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.ID_TOKEN_HINT, idTokenHint)
            )
            .andExpect(status().isOk());
    }

    @Test
    void verifyRequestWithPushDelivery() throws Throwable {
        try (val webServer = new MockWebServer(true)) {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            registeredService.setBackchannelClientNotificationEndpoint("https://localhost:%s".formatted(webServer.getPort()));
            registeredService.setBackchannelUserCodeParameterSupported(true);
            registeredService.setGenerateRefreshToken(true);
            servicesManager.save(registeredService);
            val userCode = UUID.randomUUID().toString();
            var response = mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                        registeredService.getClientSecrets().getFirst().getValue()).getBytes(StandardCharsets.UTF_8)))
                    .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                    .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                    .queryParam(OidcConstants.BINDING_MESSAGE, UUID.randomUUID().toString())
                    .queryParam(OidcConstants.USER_CODE, userCode)
                    .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            val authRequestId = MAPPER.readValue(response, Map.class).get(OidcConstants.AUTH_REQ_ID).toString();
            assertNotNull(authRequestId);

            val verifyUrl = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL + '/' + registeredService.getClientId() + '/' + authRequestId;
            val result = mockMvc.perform(get(verifyUrl)
                    .secure(true)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            assertNotNull(result.getModelAndView());
            assertEquals(OidcConstants.CIBA_VERIFICATION_VIEW, result.getModelAndView().getViewName());
            assertTrue(result.getModelAndView().getModel().containsKey("registeredService"));
            assertTrue(result.getModelAndView().getModel().containsKey("cibaRequest"));
            assertTrue(result.getModelAndView().getModel().containsKey("bindingMessage"));
            assertTrue(result.getModelAndView().getModel().containsKey("userCodeRequired"));

            webServer.start();

            val csrfToken = (CsrfToken) result.getRequest().getAttribute("_csrf");
            mockMvc.perform(post(verifyUrl)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .queryParam("userCode", userCode)
                    .cookie(result.getResponse().getCookies())
                )
                .andExpect(status().isForbidden());
            mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + "/custom/" + OidcConstants.CIBA_URL + '/'
                        + registeredService.getClientId() + '/' + authRequestId)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .queryParam("userCode", userCode)
                    .cookie(result.getResponse().getCookies())
                )
                .andExpect(status().isForbidden());

            mockMvc.perform(post(verifyUrl)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .header(csrfToken.getHeaderName(), csrfToken.getToken())
                    .queryParam(csrfToken.getParameterName(), csrfToken.getToken())
                    .cookie(result.getResponse().getCookies())
                )
                .andExpect(status().isBadRequest());


            mockMvc.perform(post(verifyUrl)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .header(csrfToken.getHeaderName(), csrfToken.getToken())
                    .queryParam(csrfToken.getParameterName(), csrfToken.getToken())
                    .queryParam("userCode", userCode)
                    .cookie(result.getResponse().getCookies())
                )
                .andExpect(status().isOk());
        }
    }

    @Test
    void verifyStatelessRequestIsGivenTheStoredId() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        registeredService.setBackchannelClientNotificationEndpoint("https://ciba.example.org");
        servicesManager.save(registeredService);

        val compactor = new OidcCibaRequestCompactor(new DirectObjectProvider<>(defaultTicketFactory),
            PrincipalFactoryUtils.newPrincipalFactory(), new DirectObjectProvider<>(oidcConfigurationContext.getPrincipalResolver()));
        val registry = new StatelessTicketRegistry((CipherExecutor) CipherExecutor.noOp(), ticketSerializationManager,
            ticketCatalog, applicationContext, List.of(compactor));
        val context = spy(oidcConfigurationContext);
        when(context.getTicketRegistry()).thenReturn(registry);
        val controller = new OidcCibaController(context, List.of());

        val cibaRequest = controller.recordCibaRequest(CibaRequestContext.builder()
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .clientId(registeredService.getClientId())
            .clientNotificationToken(UUID.randomUUID().toString())
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser"))
            .build());
        assertTrue(cibaRequest.isStateless());
        assertEquals(cibaRequest.getId(), cibaRequest.getEncodedId());
        assertTrue(cibaRequest.getEncodedId().startsWith(OidcCibaRequest.PREFIX));

        val result = controller.initializeBackchannelVerificationRequest(registeredService.getClientId(), cibaRequest.getEncodedId());
        val modelAndView = assertInstanceOf(ModelAndView.class, result);
        assertEquals(OidcConstants.CIBA_VERIFICATION_VIEW, modelAndView.getViewName());
        assertEquals(cibaRequest.getId(), ((OidcCibaRequest) modelAndView.getModel().get("cibaRequest")).getId());
    }

    @Test
    void verifyUnauthorizedRequest() throws Throwable {
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isUnauthorized());
    }
}
