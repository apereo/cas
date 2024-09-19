package org.apereo.cas.heimdall;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
        "cas.heimdall.json.location=classpath:/policies",
        "server.port=8585"
    }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class HeimdallAuthorizationControllerTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;
    
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

    private static OAuth20AccessToken buildAccessToken(final ExpirationPolicy expirationPolicy,
                                                       final Authentication authentication) {
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getId()).thenReturn(UUID.randomUUID().toString());
        when(accessToken.getAuthentication()).thenReturn(authentication);
        when(accessToken.getExpirationPolicy()).thenReturn(expirationPolicy);
        when(accessToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(accessToken.getScopes()).thenReturn(Set.of("scope1", "scope2"));
        return accessToken;
    }

    private static OAuth20AccessToken buildAccessToken() {
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        return buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE, authentication);
    }

    private static OAuth20AccessToken buildAccessToken(final Authentication authentication) {
        return buildAccessToken(NeverExpiresExpirationPolicy.INSTANCE, authentication);
    }
}
