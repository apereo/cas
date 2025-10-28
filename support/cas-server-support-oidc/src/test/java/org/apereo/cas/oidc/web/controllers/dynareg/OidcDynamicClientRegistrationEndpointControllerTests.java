package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
class OidcDynamicClientRegistrationEndpointControllerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=false")
    class DisabledTests extends AbstractOidcTests {
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


    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.registration.dynamic-client-registration-enabled=true",
        "cas.authn.oidc.registration.client-secret-expiration=P14D"
    })
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyBadEndpointRequest() throws Throwable {
            mockMvc
                .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .content("{}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(r -> {
                        r.setServerName("sso2.example.org");
                        return r;
                    })
                )
                .andExpect(status().isUnauthorized());


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
                    })
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .content("bad-input")
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyBadRedirect() throws Throwable {
            val registrationReq = """
                {
                   "redirect_uris": ["https://client.example.org/callback#something", "https://client.example.org/callback2"],
                   "request_uris": ["https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"]
                  }
                """;

            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);

            mockMvc
                .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .content(registrationReq)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyOperation() throws Throwable {
            try (val webServer = new MockWebServer(org.springframework.http.HttpStatus.OK)) {
                webServer.responseBodyJson(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
                webServer.start();

                val clientId = UUID.randomUUID().toString();
                val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
                ticketRegistry.addTicket(accessToken);

                val registrationReq = """
                    {
                        "application_type": "web",
                        "default_acr_values": ["mfa-duo","mfa-gauth"],
                        "redirect_uris": ["https://client.example.org/callback","https://client.example.org/callback2"],
                        "client_name": "My Example",
                        "client_name#ja-Japan-JP": "Japanese",
                        "logo_uri": "https://client.example.org/logo.png",
                        "policy_uri": "https://client.example.org/policy",
                        "tos_uri": "https://client.example.org/tos",
                        "subject_type": "pairwise",
                        "sector_identifier_uri": "http://localhost:%s",
                        "token_endpoint_auth_method": "client_secret_basic",
                        "jwks_uri": "https://client.example.org/my_public_keys.jwks",
                        "id_token_signed_response_alg": "RS256",
                        "id_token_encrypted_response_alg": "RSA1_5",
                        "id_token_encrypted_response_enc": "A128CBC-HS256",
                        "userinfo_encrypted_response_alg": "RSA1_5",
                        "userinfo_encrypted_response_enc": "A128CBC-HS256",
                        "contacts": ["ve7jtb@example.org", "mary@example.org"]
                    }
                    """.formatted(webServer.getPort());

                mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(withHttpRequestProcessor())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                        .content(registrationReq)
                    )
                    .andExpect(status().isCreated());
            }
        }

        @Test
        void verifyNoClientNameOperation() throws Throwable {
            try (val webServer = new MockWebServer(org.springframework.http.HttpStatus.OK)) {
                webServer.responseBodyJson(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
                webServer.start();

                val clientId = UUID.randomUUID().toString();
                val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
                ticketRegistry.addTicket(accessToken);

                val registrationReq = """
                        {
                            "application_type": "web",
                            "default_acr_values": ["mfa-duo", "mfa-gauth"],
                            "redirect_uris": ["https://client.example.org/callback", "https://client.example.org/callback2"],
                            "client_name#ja-Japan-JP": "Japanese",
                            "logo_uri": "https://client.example.org/logo.png",
                            "policy_uri": "https://client.example.org/policy",
                            "tos_uri": "https://client.example.org/tos",
                            "subject_type": "pairwise",
                            "sector_identifier_uri": "http://localhost:%s",
                            "token_endpoint_auth_method": "client_secret_basic",
                            "jwks": {"keys": []},
                            "id_token_signed_response_alg": "RS256",
                            "id_token_encrypted_response_alg": "RSA1_5",
                            "id_token_encrypted_response_enc": "A128CBC-HS256",
                            "userinfo_encrypted_response_alg": "RSA1_5",
                            "contacts": ["ve7jtb@example.org", "mary@example.org"]
                        }
                    """.formatted(webServer.getPort());

                mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(withHttpRequestProcessor())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                        .content(registrationReq)
                    )
                    .andExpect(status().isCreated());
            }
        }

        @Test
        void verifyMissingBackchannelEndpoint() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);

            try (val webServer = new MockWebServer(org.springframework.http.HttpStatus.OK)) {
                webServer.responseBodyJson(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
                webServer.start();

                var registrationReq = """
                        {
                            "application_type": "web",
                            "backchannel_token_delivery_mode": "ping",
                            "default_acr_values": ["mfa-duo", "mfa-gauth"],
                            "redirect_uris": ["https://client.example.org/callback", "https://client.example.org/callback2"],
                            "contacts": ["ve7jtb@example.org", "mary@example.org"]
                        }
                    """;
                mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(withHttpRequestProcessor())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                        .content(registrationReq)
                    )
                    .andExpect(status().isBadRequest());

                registrationReq = """
                        {
                            "application_type": "web",
                            "backchannel_token_delivery_mode": "ping",
                            "backchannel_client_notification_endpoint": "http://localhost:9811",
                            "default_acr_values": ["mfa-duo", "mfa-gauth"],
                            "redirect_uris": ["https://client.example.org/callback", "https://client.example.org/callback2"],
                            "contacts": ["ve7jtb@example.org", "mary@example.org"]
                        }
                    """;
                mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(withHttpRequestProcessor())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                        .content(registrationReq)
                    )
                    .andExpect(status().isBadRequest());
            }
        }
    }
}
