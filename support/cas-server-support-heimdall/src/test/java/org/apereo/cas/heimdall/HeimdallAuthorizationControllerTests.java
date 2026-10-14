package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import org.apereo.cas.heimdall.services.HeimdallRegisteredServiceAccessStrategy;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.idtoken.IdTokenGeneratorService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import com.nimbusds.openid.connect.sdk.Nonce;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link HeimdallAuthorizationControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Authorization")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = BaseHeimdallTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/heimdalloidc.jwks",
        "cas.authn.oidc.core.authentication-context-reference-mappings=something->mfa-something",
        "cas.heimdall.json.location=classpath:/policies"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class HeimdallAuthorizationControllerTests {
    private static final String X509_CERTIFICATE_ATTRIBUTE = "jakarta.servlet.request.X509Certificate";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
    private JwtBuilder accessTokenJwtBuilder;

    @Autowired
    @Qualifier("oidcServiceJsonWebKeystoreCache")
    private LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache;
    
    @Autowired
    @Qualifier("oidcIdTokenGenerator")
    private IdTokenGeneratorService oidcIdTokenGenerator;

    @Test
    void verifyJwtBearerToken() throws Throwable {
        val registeredService = newAssertionClient();
        val bearerToken = signAssertion(registeredService, assertionClaims(registeredService, Duration.ofMinutes(2)));
        mockMvc.perform(legacyUsersRequest(bearerToken)).andExpect(status().isOk());
        mockMvc.perform(legacyUsersRequest(bearerToken)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {JWTClaimNames.JWT_ID, JWTClaimNames.ISSUED_AT, "lifetime"})
    void verifyInvalidJwtBearerToken(final String defect) throws Throwable {
        val registeredService = newAssertionClient();
        val lifetime = "lifetime".equals(defect) ? Duration.ofMinutes(10) : Duration.ofMinutes(2);
        val claims = assertionClaims(registeredService, lifetime);
        claims.remove(defect);
        mockMvc.perform(legacyUsersRequest(signAssertion(registeredService, claims))).andExpect(status().isUnauthorized());
    }

    @Test
    void verifyIdToken() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of(
                "color", List.of("red", "green"),
                "memberOf", List.of("admin"),
                AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, List.of("mfa-something")
            )
        );
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal,
            Map.of(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), List.of("something")));
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);
        val registeredService = findRegisteredService(accessToken);

        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(accessToken)
            .responseType(OAuth20ResponseTypes.CODE)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .registeredService(registeredService)
            .build();
        val idToken = oidcIdTokenGenerator.generate(idTokenContext);
        assertNotNull(idToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/claims")
                .method("PUT")
                .namespace("API_CLAIMS")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(idToken.token()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        registeredService.setAudience(Set.of("different-client"));
        val wrongAudience = oidcIdTokenGenerator.generate(idTokenContext);
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + wrongAudience.token()))
            .andExpect(status().isUnauthorized());

    }

    @Test
    void verifyAccessTokenAsJwt() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("casuser")
            .jwtId(accessToken.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience(Set.of(accessToken.getClientId()))
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = accessTokenJwtBuilder.build(payload);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/users")
                .method("POST")
                .namespace("API_USERS")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(jwt))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyOkayOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/users")
                .method("POST")
                .namespace("API_USERS")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyGrouperGroupPolicy() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/grouper")
                .method("POST")
                .namespace("API_GROUPER")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void verifyRestfulPolicy() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        try (val webServer = new MockWebServer(9581, HttpStatus.OK)) {
            webServer.start();
            mockMvc.perform(post("/heimdall/authorize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(AuthorizationRequest.builder()
                    .uri("/api/rest")
                    .method("POST")
                    .namespace("API_REST")
                    .build()
                    .toJson()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());
        }
    }

    @Test
    void verifyGroovyOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/groovy")
                .method("POST")
                .namespace("API_GROOVY")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyAllPoliciesForcedOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/all")
                .method("POST")
                .namespace("API_ALL")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({"admin,200", "nothing,403"})
    void verifyAnyPolicyOperation(final String memberOf, final int expectedStatus) throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("memberOf", List.of(memberOf)));
        val accessToken = buildAccessToken(RegisteredServiceTestUtils.getAuthentication(principal));
        ticketRegistry.addTicket(accessToken);
        try {
            mockMvc.perform(post("/heimdall/authorize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(AuthorizationRequest.builder().uri("/api/any").method("POST").namespace("API_ANY").build().toJson())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus));
        } finally {
            ticketRegistry.deleteTicket(accessToken.getId());
        }
    }

    @Test
    void verifyScopesOperation() throws Throwable {
        val accessToken = buildAccessToken();
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/scopes")
                .method("POST")
                .namespace("API_SCOPES")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyBasicUserAuthentication() throws Throwable {
        val credentials = EncodingUtils.encodeBase64("casuser:resusac");
        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/basic")
                .method("POST")
                .namespace("API_BASIC")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Basic %s".formatted(credentials))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyUnauthorizedOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("nothing")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/users")
                .method("POST")
                .namespace("API_USERS")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void verifyUnknownOperation() throws Throwable {
        val accessToken = buildAccessToken();
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/users")
                .method("POST")
                .namespace("API_UNKNOWN")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    void verifyBadTokenOperation() throws Throwable {
        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/users")
                .method("POST")
                .namespace("API_UNKNOWN")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer 1234567890")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void verifyExpiredAccessToken() throws Throwable {
        val accessToken = buildAccessToken();
        when(accessToken.isExpired()).thenReturn(true);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/users")
                .method("POST")
                .namespace("API_UNKNOWN")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void verifyUnknownAuthHeader() throws Throwable {
        mockMvc.perform(post("/heimdall/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder()
                .uri("/api/users")
                .method("POST")
                .namespace("API_UNKNOWN")
                .build()
                .toJson()
            )
            .header(HttpHeaders.AUTHORIZATION, "Unknown User")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void verifyAuthZenRequest() throws Throwable {
        val credentials = clientCredentials();
        mockMvc.perform(post("/heimdall/authzen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(AuthorizationRequest.builder()
                    .subject(AuthZenSubject.builder().id("casperson").type("user").build())
                    .resource(AuthZenResource.builder().id("7240d0db").type("entity").build())
                    .action(AuthZenAction.builder().name("can_read").build())
                    .build()
                    .toJson()
                )
                .header(HttpHeaders.AUTHORIZATION, credentials)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(true));
    }

    @Test
    void verifyAuthZenRequestFails() throws Throwable {
        val credentials = EncodingUtils.encodeBase64("casuser:badpassword");
        mockMvc.perform(post("/heimdall/authzen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(AuthorizationRequest.builder()
                    .subject(AuthZenSubject.builder().id("casperson").type("user").build())
                    .resource(AuthZenResource.builder().id("7240d0db").type("entity").build())
                    .action(AuthZenAction.builder().name("can_read").build())
                    .build()
                    .toJson()
                )
                .header(HttpHeaders.AUTHORIZATION, "Basic %s".formatted(credentials))
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
    }

    @ParameterizedTest
    @CsvSource({"entity,can_delete", "other,can_read"})
    void verifyAuthZenResourceBoundaries(final String type, final String action) throws Throwable {
        val request = authZenRequest().withResource(AuthZenResource.builder().id("7240d0db").type(type).build())
            .withAction(AuthZenAction.builder().name(action).build())
            .withNamespace("API_USERS").withMethod("POST").withUri("/api/users");
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON)
            .content(request.toJson()).header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.decision").value(false));
    }

    @Test
    void verifyAuthZenUnknownResource() throws Throwable {
        val request = authZenRequest().withResource(AuthZenResource.builder().id(UUID.randomUUID().toString()).type("document").build());
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON)
            .content(request.toJson()).header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.decision").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"subject\":{\"id\":\"alice\"},\"resource\":{},\"action\":{}}"})
    void verifyAuthZenMalformedRequest(final String body) throws Throwable {
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyAuthZenMissingAuthentication() throws Throwable {
        val algs = String.join(" ", casProperties.getAuthn().getOidc().getDiscovery().getDpopSigningAlgValuesSupported());
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson()))
            .andExpect(status().isUnauthorized())
            .andExpect(header().stringValues(HttpHeaders.WWW_AUTHENTICATE,
                org.hamcrest.Matchers.hasItem("%s algs=\"%s\"".formatted(OAuth20Constants.TOKEN_TYPE_DPOP, algs))));
    }

    @Test
    void verifyUntrustedTokenClient() throws Throwable {
        val token = buildAccessToken();
        when(token.getClientId()).thenReturn(UUID.randomUUID().toString());
        ticketRegistry.addTicket(token);
        try {
            mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId()))
                .andExpect(status().isUnauthorized());
        } finally {
            ticketRegistry.deleteTicket(token.getId());
        }
    }

    @Test
    void verifyAuthZenClientCredentials() throws Throwable {
        val registeredService = newOidcRegisteredService("heimdall-" + UUID.randomUUID());
        val credentials = clientCredentials(registeredService, "s3cr:et:" + UUID.randomUUID());
        mockMvc.perform(authZenRequest(credentials))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(true));
        mockMvc.perform(authZenRequest("Basic " + EncodingUtils.encodeBase64(registeredService.getClientId() + ":wrong")))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(authZenRequest("Basic " + EncodingUtils.encodeBase64("casuser:resusac")))
            .andExpect(status().isUnauthorized());
        registeredService.setAccessStrategy(new HeimdallRegisteredServiceAccessStrategy().setAllowed(false));
        servicesManager.save(registeredService);
        mockMvc.perform(authZenRequest(credentials))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyAuthZenIgnoresUnknownFields() throws Throwable {
        val body = """
            {
              "subject": {"type": "user", "id": "casperson", "unknown": 1},
              "resource": {"type": "entity", "id": "7240d0db", "unknown": true},
              "action": {"name": "can_read", "properties": {"method": "GET"}},
              "context": {"time": "2026-01-01T00:00:00Z"},
              "future_field": {"name": "value"}
            }
            """;
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(true));
    }

    @ParameterizedTest
    @CsvSource({"user,true", "service,false"})
    void verifyAuthZenSubjectTypeResolution(final String subjectType, final boolean decision) throws Throwable {
        val request = authZenRequest().withSubject(AuthZenSubject.builder().type(subjectType).id("casperson").build());
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(request.toJson())
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(decision));
    }

    @ParameterizedTest
    @CsvSource({"Finance,true", "Sales,false"})
    void verifyAuthZenSubjectProperties(final String department, final boolean decision) throws Throwable {
        val request = authZenRequest()
            .withSubject(AuthZenSubject.builder().type("service").id("billing-api").properties(Map.of("department", department)).build())
            .withResource(AuthZenResource.builder().type("invoice").id("inv-1").build());
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(request.toJson())
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(decision));
    }

    @Test
    void verifyAuthZenContextIgnoresHttpHeaders() throws Throwable {
        val request = authZenRequest().withResource(AuthZenResource.builder().type("channel").id("c-1").build());
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON)
                .content(request.withContext(new HashMap<>(Map.of("channel", "web"))).toJson())
                .header("channel", "api")
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(true));
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON)
                .content(request.toJson())
                .header("channel", "web")
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(false));
    }

    @Test
    void verifyLegacyContextHeaders() throws Throwable {
        val credentials = "Basic " + EncodingUtils.encodeBase64("casuser:resusac");
        val request = AuthorizationRequest.builder().namespace("API_QUALIFIED").method("POST").uri("/api/headers").build();
        mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON).content(request.toJson())
                .header("X-Original-URI", "/api/protected")
                .header(HttpHeaders.AUTHORIZATION, credentials))
            .andExpect(status().isOk());
        mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON)
                .content(request.withContext(new HashMap<>(Map.of("X-Original-URI", "/api/elsewhere"))).toJson())
                .header("X-Original-URI", "/api/protected")
                .header(HttpHeaders.AUTHORIZATION, credentials))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON)
                .content(request.withUri("/api/protocol").toJson())
                .header(HttpHeaders.AUTHORIZATION, credentials))
            .andExpect(status().isForbidden());
    }

    @Test
    void verifyAuthZenPublicClientIsRejected() throws Throwable {
        val registeredService = newOidcRegisteredService("heimdall-" + UUID.randomUUID());
        servicesManager.save(registeredService);
        mockMvc.perform(authZenRequest("Basic " + EncodingUtils.encodeBase64(registeredService.getClientId() + ":anything")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyResourceWithoutPoliciesIsDenied() throws Throwable {
        val request = authZenRequest()
            .withResource(AuthZenResource.builder().type("unguarded").id("anything").build());
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(request.toJson())
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(false));
        mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON)
                .content(AuthorizationRequest.builder().namespace("API_UNGUARDED").uri("/api/unguarded").method("POST").build().toJson())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("casuser:resusac")))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
        "entity,7240d0db,can_read,true",
        "document,doc-1,can_read,true",
        "document,doc-1,can_write,true",
        "document,doc-x,can_read,false",
        "document,xdoc-1,can_read,false",
        "document,doc-9,can_read,false"
    })
    void verifyAuthZenResourcesAcrossNamespaces(final String type, final String id,
                                                final String action, final boolean decision)
            throws Throwable {
        val request = authZenRequest()
            .withResource(AuthZenResource.builder().type(type).id(id).build())
            .withAction(AuthZenAction.builder().name(action).build());
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(request.toJson())
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(decision));
    }

    @Test
    void verifyAuthZenConfiguration() throws Throwable {
        val policyDecisionPoint = casProperties.getServer().getPrefix() + "/heimdall";
        mockMvc.perform(get("/heimdall/.well-known/authzen-configuration").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.policy_decision_point").value(policyDecisionPoint))
            .andExpect(jsonPath("$.access_evaluation_endpoint").value(policyDecisionPoint + "/authzen"))
            .andExpect(jsonPath("$.access_evaluations_endpoint").value(policyDecisionPoint + "/authzen/evaluations"))
            .andExpect(jsonPath("$.search_subject_endpoint").doesNotExist())
            .andExpect(jsonPath("$.capabilities").doesNotExist());
    }

    @Test
    void verifyAuthZenEvaluations() throws Throwable {
        val body = """
            {
              "subject": {"type": "user", "id": "casperson"},
              "action": {"name": "can_read"},
              "context": {"channel": "web"},
              "evaluations": [
                {"resource": {"type": "entity", "id": "1"}},
                {"resource": {"type": "document", "id": "doc-9"}},
                {"action": {"name": "can_write"}, "resource": {"type": "entity", "id": "1"}},
                {"resource": {"type": "entity"}},
                {"subject": {"type": "user", "id": "casuser"}, "resource": {"type": "entity", "id": "2"}}
              ]
            }
            """;
        val requestId = UUID.randomUUID().toString();
        mockMvc.perform(authZenEvaluationsRequest(body, clientCredentials()).header("X-Request-ID", requestId))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-ID", requestId))
            .andExpect(jsonPath("$.evaluations.length()").value(5))
            .andExpect(jsonPath("$.evaluations[0].decision").value(true))
            .andExpect(jsonPath("$.evaluations[0].context").doesNotExist())
            .andExpect(jsonPath("$.evaluations[1].decision").value(false))
            .andExpect(jsonPath("$.evaluations[1].context.reason").value("policy_denied"))
            .andExpect(jsonPath("$.evaluations[2].decision").value(false))
            .andExpect(jsonPath("$.evaluations[2].context.reason").value("no_matching_resource"))
            .andExpect(jsonPath("$.evaluations[3].decision").value(false))
            .andExpect(jsonPath("$.evaluations[3].context.error.status").value(400))
            .andExpect(jsonPath("$.evaluations[4].decision").value(true));
    }

    @ParameterizedTest
    @CsvSource({"deny_on_first_deny,2,false", "permit_on_first_permit,2,true", "execute_all,3,true"})
    void verifyAuthZenEvaluationsSemantics(final String semantic, final int count, final boolean last) throws Throwable {
        val first = "deny_on_first_deny".equals(semantic) ? "entity" : "document";
        val second = "deny_on_first_deny".equals(semantic) ? "document" : "entity";
        val body = """
            {
              "subject": {"type": "user", "id": "casperson"},
              "action": {"name": "can_read"},
              "options": {"evaluations_semantic": "%s"},
              "evaluations": [
                {"resource": {"type": "%s", "id": "doc-9"}},
                {"resource": {"type": "%s", "id": "doc-9"}},
                {"resource": {"type": "entity", "id": "3"}}
              ]
            }
            """.formatted(semantic, first, second);
        mockMvc.perform(authZenEvaluationsRequest(body, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.evaluations.length()").value(count))
            .andExpect(jsonPath("$.evaluations[%s].decision".formatted(count - 1)).value(last));
    }

    @Test
    void verifyAuthZenEvaluationsWithoutEntries() throws Throwable {
        val body = """
            {
              "subject": {"type": "user", "id": "casperson"},
              "action": {"name": "can_read"},
              "resource": {"type": "entity", "id": "1"},
              "evaluations": []
            }
            """;
        mockMvc.perform(authZenEvaluationsRequest(body, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(true))
            .andExpect(jsonPath("$.evaluations").doesNotExist());
        mockMvc.perform(authZenEvaluationsRequest(body.replace("\"id\": \"1\"", "\"id\": \"\""), clientCredentials()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyAuthZenEvaluationsFailures() throws Throwable {
        val body = """
            {
              "subject": {"type": "user", "id": "casperson"},
              "action": {"name": "can_read"},
              "evaluations": [{"resource": {"type": "entity", "id": "1"}}]
            }
            """;
        mockMvc.perform(post("/heimdall/authzen/evaluations").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
        mockMvc.perform(authZenEvaluationsRequest(body, "Basic " + EncodingUtils.encodeBase64("casuser:resusac")))
            .andExpect(status().isUnauthorized());
        val unknownSemantic = body.replace("\"evaluations\":", "\"options\": {\"evaluations_semantic\": \"unknown\"}, \"evaluations\":");
        mockMvc.perform(authZenEvaluationsRequest(unknownSemantic, clientCredentials()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyAuthZenEvaluationsConsumeAssertionOnce() throws Throwable {
        val registeredService = newAssertionClient();
        val assertion = "Bearer " + signAssertion(registeredService, assertionClaims(registeredService, Duration.ofMinutes(2)));
        val body = """
            {
              "subject": {"type": "user", "id": "casperson"},
              "action": {"name": "can_read"},
              "evaluations": [
                {"resource": {"type": "entity", "id": "1"}},
                {"resource": {"type": "entity", "id": "2"}}
              ]
            }
            """;
        mockMvc.perform(authZenEvaluationsRequest(body, assertion))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.evaluations[0].decision").value(true))
            .andExpect(jsonPath("$.evaluations[1].decision").value(true));
        mockMvc.perform(authZenEvaluationsRequest(body, assertion)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
        "entity,7240d0db,can_read,",
        "document,doc-9,can_read,policy_denied",
        "entity,7240d0db,can_write,no_matching_resource",
        "unguarded,anything,can_read,no_policies"
    })
    void verifyAuthZenDecisionContext(final String type, final String id, final String action,
                                      final @Nullable String reason)
            throws Throwable {
        val request = authZenRequest()
            .withResource(AuthZenResource.builder().type(type).id(id).build())
            .withAction(AuthZenAction.builder().name(action).build());
        val result = mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(request.toJson())
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decision").value(reason == null));
        if (reason == null) {
            result.andExpect(jsonPath("$.context").doesNotExist());
        } else {
            result.andExpect(jsonPath("$.context.reason").value(reason));
        }
    }

    @Test
    void verifyAuthZenRequestIdIsEchoed() throws Throwable {
        val requestId = UUID.randomUUID().toString();
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson())
                .header("X-Request-ID", requestId)
                .header(HttpHeaders.AUTHORIZATION, clientCredentials()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-ID", requestId));
        mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson())
                .header("X-Request-ID", requestId))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("X-Request-ID", requestId));
    }

    @Test
    void verifyAuthorizeRejectsAuthZenFields() throws Throwable {
        val request = authZenRequest().withNamespace("API_USERS").withMethod("POST").withUri("/api/users");
        mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON).content(request.toJson())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("casuser:resusac")))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyHeimdallAccessStrategy(final boolean chained) throws Throwable {
        val registeredService = newOidcRegisteredService("heimdall-" + UUID.randomUUID());
        val strategy = new HeimdallRegisteredServiceAccessStrategy().setAllowed(false);
        if (chained) {
            val chain = new ChainingRegisteredServiceAccessStrategy();
            chain.addStrategies(new DefaultRegisteredServiceAccessStrategy(), strategy);
            registeredService.setAccessStrategy(chain);
        } else {
            registeredService.setAccessStrategy(strategy);
        }
        val token = buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE,
            RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), registeredService);
        ticketRegistry.addTicket(token);
        try {
            mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId()))
                .andExpect(status().isUnauthorized());
            strategy.setAllowed(true);
            servicesManager.save(registeredService);
            mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value(true));
        } finally {
            ticketRegistry.deleteTicket(token.getId());
        }
    }

    @Test
    void verifyDisabledServiceCannotCallHeimdall() throws Throwable {
        val registeredService = newOidcRegisteredService("heimdall-" + UUID.randomUUID());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        val token = buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE,
            RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), registeredService);
        ticketRegistry.addTicket(token);
        try {
            mockMvc.perform(post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON).content(authZenRequest().toJson())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId()))
                .andExpect(status().isUnauthorized());
        } finally {
            ticketRegistry.deleteTicket(token.getId());
        }
    }

    @Test
    void verifyHeimdallAccessStrategySerialization() {
        val registeredService = newOidcRegisteredService("heimdall-" + UUID.randomUUID());
        registeredService.setAccessStrategy(new HeimdallRegisteredServiceAccessStrategy().setAllowed(false));
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val json = serializer.toString(registeredService);
        assertEquals(registeredService, serializer.from(json));
    }

    @Test
    void verifyCertificateBoundAccessToken() throws Throwable {
        val boundEncoding = RandomUtils.randomAlphanumeric(64).getBytes(StandardCharsets.UTF_8);
        val digest = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(boundEncoding));
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal,
            Map.of(OAuth20Constants.X509_CERTIFICATE_DIGEST, List.of(digest)));
        val token = buildAccessToken(authentication);
        ticketRegistry.addTicket(token);
        val body = AuthorizationRequest.builder().namespace("API_USERS").uri("/api/users").method("POST").build().toJson();
        val boundCertificate = mock(X509Certificate.class);
        when(boundCertificate.getEncoded()).thenReturn(boundEncoding);
        val otherCertificate = mock(X509Certificate.class);
        when(otherCertificate.getEncoded()).thenReturn(RandomUtils.randomAlphanumeric(64).getBytes(StandardCharsets.UTF_8));
        try {
            mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON).content(body)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId()))
                .andExpect(status().isUnauthorized());
            mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON).content(body)
                    .requestAttr(X509_CERTIFICATE_ATTRIBUTE, new X509Certificate[]{otherCertificate})
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId()))
                .andExpect(status().isUnauthorized());
            mockMvc.perform(post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON).content(body)
                    .requestAttr(X509_CERTIFICATE_ATTRIBUTE, new X509Certificate[]{boundCertificate})
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId()))
                .andExpect(status().isOk());
        } finally {
            ticketRegistry.deleteTicket(token.getId());
        }
    }

    @ParameterizedTest
    @CsvSource({"false,/heimdall/authzen", "true,/heimdall/authzen", "false,/heimdall/authorize", "true,/heimdall/authorize"})
    void verifySenderConstrainedTokens(final boolean jwtToken, final String endpoint) throws Throwable {
        val key = new ECKeyGenerator(Curve.P_256).generate();
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal,
            Map.of(OAuth20Constants.DPOP_CONFIRMATION, List.of(key.computeThumbprint().toString())));
        val token = buildAccessToken(authentication);
        ticketRegistry.addTicket(token);
        try {
            val presentedToken = jwtToken ? accessTokenJwtBuilder.build(JwtBuilder.JwtRequest.builder()
                .subject(principal.getId()).jwtId(token.getId()).issuer(casProperties.getServer().getPrefix())
                .serviceAudience(Set.of(token.getClientId()))
                .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1))).build()) : token.getId();
            val body = endpoint.endsWith("authzen") ? authZenRequest().toJson()
                : AuthorizationRequest.builder().namespace("API_USERS").uri("/api/users").method("POST").build().toJson();
            val target = URI.create("http://localhost" + endpoint);
            val factory = new DefaultDPoPProofFactory(key, JWSAlgorithm.ES256);
            val wrongKeyFactory = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256);
            mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + presentedToken))
                .andExpect(status().isUnauthorized());
            mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, "DPoP " + presentedToken))
                .andExpect(status().isUnauthorized());
            val invalidProofs = List.of(
                wrongKeyFactory.createDPoPJWT("POST", target, new DPoPAccessToken(presentedToken)),
                factory.createDPoPJWT("POST", target, new DPoPAccessToken("wrong-token")),
                factory.createDPoPJWT("GET", target, new DPoPAccessToken(presentedToken)),
                factory.createDPoPJWT("POST", URI.create("http://localhost/another-endpoint"), new DPoPAccessToken(presentedToken)));
            for (val proof : invalidProofs) {
                mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body)
                    .header(HttpHeaders.AUTHORIZATION, "DPoP " + presentedToken).header("DPoP", proof.serialize()))
                    .andExpect(status().isUnauthorized());
            }
            val proof = factory.createDPoPJWT("POST", target, new DPoPAccessToken(presentedToken));
            mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, "DPoP " + presentedToken).header("DPoP", proof.serialize()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.decision").value(true));
            mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, "DPoP " + presentedToken).header("DPoP", proof.serialize()))
                .andExpect(status().isUnauthorized());
        } finally {
            ticketRegistry.deleteTicket(token.getId());
        }
    }

    /**
     * Once DPoP nonces are turned on, a caller's DPoP proof must carry one handed out by CAS.
     */
    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.dpop.nonce.enabled=true")
    class DPoPNonceTests {
        @Test
        void verifySenderConstrainedTokenRequiresNonce() throws Throwable {
            val key = new ECKeyGenerator(Curve.P_256).generate();
            val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("memberOf", List.of("admin")));
            val token = buildAccessToken(RegisteredServiceTestUtils.getAuthentication(principal,
                Map.of(OAuth20Constants.DPOP_CONFIRMATION, List.of(key.computeThumbprint().toString()))));
            ticketRegistry.addTicket(token);
            try {
                val target = URI.create("http://localhost/heimdall/authzen");
                val factory = new DefaultDPoPProofFactory(key, JWSAlgorithm.ES256);
                val presentedToken = new DPoPAccessToken(token.getId());
                val nonce = mockMvc.perform(authZenRequest("DPoP " + token.getId())
                        .header(OAuth20Constants.DPOP, factory.createDPoPJWT("POST", target, presentedToken).serialize()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
                        org.hamcrest.Matchers.containsString("error=\"" + OAuth20Constants.USE_DPOP_NONCE + '"')))
                    .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, org.hamcrest.Matchers.containsString("algs=\"%s\"".formatted(
                        String.join(" ", casProperties.getAuthn().getOidc().getDiscovery().getDpopSigningAlgValuesSupported())))))
                    .andReturn().getResponse().getHeader(OAuth20Constants.DPOP_NONCE);
                assertNotNull(nonce);
                mockMvc.perform(authZenRequest("DPoP " + token.getId())
                        .header(OAuth20Constants.DPOP, factory.createDPoPJWT("POST", target, presentedToken, new Nonce(nonce)).serialize()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.decision").value(true));
            } finally {
                ticketRegistry.deleteTicket(token.getId());
            }
        }
    }

    private static MockHttpServletRequestBuilder authZenRequest(final String authorizationHeader) {
        return post("/heimdall/authzen").contentType(MediaType.APPLICATION_JSON)
            .content(authZenRequest().toJson())
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader);
    }
    
    private static AuthorizationRequest authZenRequest() {
        return AuthorizationRequest.builder()
            .subject(AuthZenSubject.builder().id("casperson").type("user").build())
            .resource(AuthZenResource.builder().id("7240d0db").type("entity").build())
            .action(AuthZenAction.builder().name("can_read").build()).build();
    }

    private static MockHttpServletRequestBuilder authZenEvaluationsRequest(final String body, final String authorizationHeader) {
        return post("/heimdall/authzen/evaluations").contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader);
    }

    private String clientCredentials() {
        return clientCredentials(newOidcRegisteredService("heimdall-" + UUID.randomUUID()), UUID.randomUUID().toString());
    }

    private String clientCredentials(final OidcRegisteredService registeredService, final String clientSecret) {
        registeredService.setClientSecret(clientSecret);
        servicesManager.save(registeredService);
        return "Basic " + EncodingUtils.encodeBase64(registeredService.getClientId() + ':' + clientSecret);
    }

    private OidcRegisteredService newAssertionClient() {
        val registeredService = newOidcRegisteredService("heimdall-assertion-" + UUID.randomUUID());
        registeredService.setJwks("classpath:keystore.jwks");
        servicesManager.save(registeredService);
        return registeredService;
    }

    private Map<String, Object> assertionClaims(final OidcRegisteredService registeredService, final Duration lifetime) {
        val now = Instant.now(Clock.systemUTC()).getEpochSecond();
        return new HashMap<>(Map.of(
            "memberOf", "my-admin",
            JWTClaimNames.JWT_ID, UUID.randomUUID().toString(),
            JWTClaimNames.ISSUED_AT, now,
            OAuth20Constants.CLAIM_EXP, now + lifetime.toSeconds(),
            OAuth20Constants.CLAIM_SUB, "casuser",
            OidcConstants.ISS, registeredService.getClientId(),
            OAuth20Constants.CLIENT_ID, registeredService.getClientId(),
            OidcConstants.AUD, casProperties.getServer().getPrefix() + '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL));
    }

    private String signAssertion(final OidcRegisteredService registeredService, final Map<String, Object> claims) throws Throwable {
        val jsonWebKey = (PublicJsonWebKey) oidcServiceJsonWebKeystoreCache
            .get(new OidcJsonWebKeyCacheKey(registeredService, OidcJsonWebKeyUsage.SIGNING))
            .orElseThrow()
            .getJsonWebKeys()
            .getFirst();
        val signed = EncodingUtils.signJwsRSASha512(jsonWebKey.getPrivateKey(),
            JWTClaimsSet.parse(claims).toString().getBytes(StandardCharsets.UTF_8), Map.of());
        return new String(signed, StandardCharsets.UTF_8);
    }

    private static MockHttpServletRequestBuilder legacyUsersRequest(final String bearerToken) {
        return post("/heimdall/authorize").contentType(MediaType.APPLICATION_JSON)
            .content(AuthorizationRequest.builder().uri("/api/users").method("POST").namespace("API_USERS").build().toJson())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .accept(MediaType.APPLICATION_JSON);
    }

    private OAuth20AccessToken buildAccessToken(final ExpirationPolicy expirationPolicy,
                                                final Authentication authentication) {
        return buildAccessToken(expirationPolicy, authentication, newOidcRegisteredService("heimdall-" + UUID.randomUUID()));
    }

    private OAuth20AccessToken buildAccessToken(final ExpirationPolicy expirationPolicy,
                                                final Authentication authentication,
                                                final OidcRegisteredService registeredService) {
        servicesManager.save(registeredService);
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getId()).thenReturn("%s-%s".formatted(OAuth20AccessToken.PREFIX, UUID.randomUUID().toString()));
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(accessToken.getExpirationPolicy()).thenReturn(expirationPolicy);
        when(accessToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(accessToken.getScopes()).thenReturn(Set.of("scope1", "scope2", OidcConstants.StandardScopes.OPENID.getScope()));
        when(accessToken.getClientId()).thenReturn(registeredService.getClientId());
        return accessToken;
    }

    private OAuth20AccessToken buildAccessToken() {
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        return buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE, authentication);
    }

    private OAuth20AccessToken buildAccessToken(final Authentication authentication) {
        return buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE, authentication);
    }

    private @Nullable OidcRegisteredService findRegisteredService(final OAuth20AccessToken accessToken) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, accessToken.getClientId(), OidcRegisteredService.class);
    }

    private static OidcRegisteredService newOidcRegisteredService(final String clientId) {
        val oidcRegisteredService = new OidcRegisteredService();
        oidcRegisteredService.setClientId(clientId);
        oidcRegisteredService.setServiceId("https://example.org");
        oidcRegisteredService.setName(clientId);
        return oidcRegisteredService;
    }
}
