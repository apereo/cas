package org.apereo.cas.oidc.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import java.util.Set;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This is {@link OidcDynamicClientRegistrationToggleTests}.
 *
 * Verifies that OIDC dynamic client registration controllers respect
 * the property
 * {@code cas.authn.oidc.registration.dynamic-client-registration-enabled}.
 *
 * When disabled, endpoints should return 404.
 *
 * @author Jiří Prokop
 * @since 7.3.0
 */
@Tag("OIDC")
class OidcDynamicClientRegistrationToggleTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=true")
    class EnabledByDefault extends AbstractOidcTests {
        @Test
        void verifyDynamicClientRegistrationEnabledByDefault() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(withHttpRequestProcessor())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                            .content("bad-input"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void verifyClientConfigurationEnabledByDefault() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(withHttpRequestProcessor())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                            .content("{}")
                            .with(r -> {
                                r.setServerName("sso2.example.org");
                                return r;
                            }))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=true")
    class ExplicitEnabled extends AbstractOidcTests {
        @Test
        void verifyDynamicClientRegistrationEnabledExplicitly() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(withHttpRequestProcessor())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                            .content("bad-input"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void verifyClientConfigurationEnabledExplicitly() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(withHttpRequestProcessor())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                            .content("{}")
                            .with(r -> {
                                r.setServerName("sso2.example.org");
                                return r;
                            }))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=false")
    class Disabled extends AbstractOidcTests {
        @Test
        void verifyDynamicClientRegistrationDisabled() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(withHttpRequestProcessor())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                            .content("bad-input"))
                    .andExpect(status().isNotImplemented());
        }

        @Test
        void verifyClientConfigurationDisabled() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(withHttpRequestProcessor())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                            .content("{}")
                            .with(r -> {
                                r.setServerName("sso2.example.org");
                                return r;
                            }))
                    .andExpect(status().isNotImplemented());
        }
    }
}
