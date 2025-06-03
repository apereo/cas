package org.apereo.cas.heimdall;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
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
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
    @Autowired
    private CasConfigurationProperties casProperties;

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
    @Qualifier("oidcIdTokenGenerator")
    private IdTokenGeneratorService oidcIdTokenGenerator;

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

        val registeredService = newOidcRegisteredService(accessToken.getClientId());
        servicesManager.save(registeredService);

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

    }

    @Test
    void verifyAccessTokenAsJwt() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            Map.of("color", List.of("red", "green"), "memberOf", List.of("admin")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val accessToken = buildAccessToken(authentication);
        ticketRegistry.addTicket(accessToken);

        val registeredService = newOidcRegisteredService(accessToken.getClientId());
        servicesManager.save(registeredService);

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
        ).andExpect(status().isForbidden());
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
        ).andExpect(status().isForbidden());
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
        ).andExpect(status().isForbidden());
    }

    @Test
    void verifyAuthZenRequest() throws Throwable {
        val credentials = EncodingUtils.encodeBase64("casuser:resusac");
        mockMvc.perform(post("/heimdall/authzen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(AuthorizationRequest.builder()
                    .subject(AuthZenSubject.builder().id("casperson").type("user").build())
                    .resource(AuthZenResource.builder().id("authzen_resource").type("entity").build())
                    .action(AuthZenAction.builder().name("can_read").build())
                    .build()
                    .toJson()
                )
                .header(HttpHeaders.AUTHORIZATION, "Basic %s".formatted(credentials))
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
                    .resource(AuthZenResource.builder().id("authzen_resource").type("entity").build())
                    .action(AuthZenAction.builder().name("can_read").build())
                    .build()
                    .toJson()
                )
                .header(HttpHeaders.AUTHORIZATION, "Basic %s".formatted(credentials))
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.decision").value(false));
    }

    private static OAuth20AccessToken buildAccessToken(final ExpirationPolicy expirationPolicy,
                                                       final Authentication authentication) {
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getId()).thenReturn("%s-%s".formatted(OAuth20AccessToken.PREFIX, UUID.randomUUID().toString()));
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(accessToken.getExpirationPolicy()).thenReturn(expirationPolicy);
        when(accessToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(accessToken.getScopes()).thenReturn(Set.of("scope1", "scope2", OidcConstants.StandardScopes.OPENID.getScope()));
        when(accessToken.getClientId()).thenReturn(UUID.randomUUID().toString());
        return accessToken;
    }

    private static OAuth20AccessToken buildAccessToken() {
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        return buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE, authentication);
    }

    private static OAuth20AccessToken buildAccessToken(final Authentication authentication) {
        return buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE, authentication);
    }

    private static OidcRegisteredService newOidcRegisteredService(final String clientId) {
        val oidcRegisteredService = new OidcRegisteredService();
        oidcRegisteredService.setClientId(clientId);
        oidcRegisteredService.setServiceId("https://example.org");
        oidcRegisteredService.setName(clientId);
        return oidcRegisteredService;
    }
}
