package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientIdMetadataDocument;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.DirectObjectProvider;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
        "cas.authn.oidc.registration.client-secret-expiration=P14D",
        "cas.authn.oidc.registration.dynamic-client-registration-mode=PROTECTED"
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

        /**
         * ID tokens are always signed, so a client asking for {@code none} as its ID token signing algorithm is not registered.
         */
        @Test
        void verifyUnsignedIdTokensAreNotRegistered() throws Throwable {
            val registrationReq = """
                {
                   "redirect_uris": ["https://client.example.org/callback"],
                   "id_token_signed_response_alg": "none"
                }
                """;
            val accessToken = getAccessToken(UUID.randomUUID().toString(), Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);

            mockMvc
                .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .content(registrationReq)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_client_metadata"));
        }
        
        @Test
        void verifyNoClientNameOperation() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);

            val registrationReq = """
                    {
                        "application_type": "web",
                        "default_acr_values": ["mfa-duo", "mfa-gauth"],
                        "redirect_uris": ["https://client.example.org/callback5", "https://client.example.org/callback11?tenant=a"],
                        "client_name#ja-Japan-JP": "Japanese",
                        "logo_uri": "https://client.example.org/logo.png",
                        "policy_uri": "https://client.example.org/policy",
                        "tos_uri": "https://client.example.org/tos",
                        "subject_type": "pairwise",
                        "token_endpoint_auth_method": "client_secret_basic",
                        "jwks": {"keys": []},
                        "id_token_signed_response_alg": "RS256",
                        "id_token_encrypted_response_alg": "RSA1_5",
                        "id_token_encrypted_response_enc": "A128CBC-HS256",
                        "userinfo_encrypted_response_alg": "RSA1_5",
                        "dpop_bound_access_tokens": true,
                        "tls_client_certificate_bound_access_tokens": true,
                        "contacts": ["ve7jtb@example.org", "mary@example.org"]
                    }
                """;

            val result = mockMvc
                .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .content(registrationReq)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dpop_bound_access_tokens").value(true))
                .andExpect(jsonPath("$.tls_client_certificate_bound_access_tokens").value(true))
                .andExpect(jsonPath("$.redirect_uris[0]").value("https://client.example.org/callback5"))
                .andExpect(jsonPath("$.redirect_uris[1]").value("https://client.example.org/callback11?tenant=a"))
                .andReturn();
            val registeredClientId = JsonPath.read(result.getResponse().getContentAsString(), "$.client_id").toString();
            val registeredClient = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, registeredClientId);
            assertTrue(registeredClient.isDpopBoundAccessTokens());
            assertTrue(registeredClient.isTlsClientCertificateBoundAccessTokens());
            assertTrue(registeredClient.matches("https://client.example.org/callback5"));
            assertTrue(registeredClient.matches("https://client.example.org/callback11?tenant=a"));
            assertFalse(registeredClient.matches("https://clientxexample.org/callback5"));
            assertFalse(registeredClient.matches("https://client.example.org/callback5/other"));
            assertTrue(PairwiseOidcRegisteredServiceUsernameAttributeProvider.hasSalt(registeredClient));
        }

        @Test
        @ResourceLock(Resources.SYSTEM_PROPERTIES)
        void verifyJwksKeyIdEvaluatesSystemProperties() throws Throwable {
            val propertyName = "cas.oidc.jwks.leak";
            val propertyValue = UUID.randomUUID().toString();
            System.setProperty(propertyName, propertyValue);
            try {
                val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
                key.setKeyId("${#systemProperties['cas.oidc.jwks.leak']}");
                val jwks = new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);

                val clientId = UUID.randomUUID().toString();
                val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
                ticketRegistry.addTicket(accessToken);

                val registrationReq = """
                    {
                        "application_type": "web",
                        "redirect_uris": ["https://client.example.org/callback"],
                        "token_endpoint_auth_method": "client_secret_basic",
                        "jwks": %s,
                        "contacts": ["cas@example.org"]
                    }
                    """.formatted(jwks);

                mockMvc
                    .perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(withHttpRequestProcessor())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                        .content(registrationReq)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.jwks").doesNotExist());
            } finally {
                System.clearProperty(propertyName);
            }
        }

        @Test
        void verifyMissingBackchannelEndpoint() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);

            try (val webServer = new MockWebServer(HttpStatus.OK)) {
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

        /**
         * A client that asks for pairwise subject identifiers with redirect URIs on several hosts must register a sector
         * identifier URI, which must be an {@code https} URL that can be fetched (OpenID Connect Core 1.0, section 8.1),
         * from a public address. Sectors are read as they are when identifiers are calculated: a web redirect URI is in the
         * sector of its host, so one that another parser cannot read still counts, any other redirect URI is in the
         * client's own sector, so mixing them needs a sector identifier URI, and a web redirect URI without a host is
         * refused.
         */
        @Test
        void verifySectorIdentifierIsRequiredAndVerified() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            val redirectUris = List.of("https://client.example.org/callback", "https://other.example.org/callback");
            try (val webServer = new MockWebServer(true)) {
                webServer.responseBodyJson(redirectUris);
                webServer.start();
                val twoHosts = "\"redirect_uris\": [\"https://client.example.org/callback\", \"https://other.example.org/callback\"]";
                for (val metadata : List.of(twoHosts,
                    "\"redirect_uris\": [\"https://client.example.org/callback\", \"https://other.example.org/callback?x={y}\"]",
                    "\"redirect_uris\": [\"https://client.example.org/callback\", \"https:///callback\"]",
                    "\"redirect_uris\": [\"https://client.example.org/callback\", \"https:/other.example.org/callback\"]",
                    "\"redirect_uris\": [\"https://client.example.org/callback\", \"com.example.app:/callback\"]",
                    twoHosts + ",\"sector_identifier_uri\": \"http://localhost:9813/sector\"",
                    twoHosts + ",\"sector_identifier_uri\": \"https:///sector\"",
                    twoHosts + ",\"sector_identifier_uri\": \"https://localhost:%s/sector\"".formatted(webServer.getPort()))) {
                    val registrationReq = """
                            {
                                "application_type": "web",
                                "subject_type": "pairwise",
                                %s
                            }
                        """.formatted(metadata);
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

            val sectorDocument = new AtomicReference<>("[\"https://client.example.org/callback\"]");
            val translator = new OidcDefaultClientRegistrationRequestTranslator(new DirectObjectProvider<>(oidcConfigurationContext),
                uri -> new OidcClientIdMetadataDocument(uri.toString(), sectorDocument.get(), Duration.ZERO));
            val registrationRequest = new OidcClientRegistrationRequest();
            registrationRequest.setRedirectUris(redirectUris);
            registrationRequest.setSubjectType("pairwise");
            registrationRequest.setSectorIdentifierUri("https://sector.example.org/sector.json");
            assertThrows(IllegalArgumentException.class, () -> translator.translate(registrationRequest, Optional.empty()));
            sectorDocument.set("[\"https://client.example.org/callback\", \"https://other.example.org/callback\",]");
            assertThrows(IllegalArgumentException.class, () -> translator.translate(registrationRequest, Optional.empty()));
            sectorDocument.set("[\"https://client.example.org/callback\", \"https://other.example.org/callback\"]");
            val registeredService = translator.translate(registrationRequest, Optional.empty());
            assertEquals("https://sector.example.org/sector.json", registeredService.getSectorIdentifierUri());
            assertInstanceOf(PairwiseOidcRegisteredServiceUsernameAttributeProvider.class, registeredService.getUsernameAttributeProvider());

            val customGenerator = mock(PersistentIdGenerator.class);
            val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
            provider.setPersistentIdGenerator(customGenerator);
            val givenService = getOidcRegisteredService(UUID.randomUUID().toString());
            givenService.setUsernameAttributeProvider(provider);
            val updatedService = translator.translate(registrationRequest, Optional.of(givenService));
            assertSame(customGenerator, ((PairwiseOidcRegisteredServiceUsernameAttributeProvider) updatedService
                .getUsernameAttributeProvider()).getPersistentIdGenerator());
        }

        @Test
        void verifyCanonicalRegistrationPathRequiresAuth() throws Throwable {
            mockMvc.perform(post("/cas/oidc/" + OidcConstants.REGISTRATION_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void verifyInjectedSegmentBypassesAuth() throws Throwable {
            mockMvc.perform(post("/cas/oidc/x/" + OidcConstants.REGISTRATION_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void verifyRegistrationAccessTokenIsTheStoredToken() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val storedToken = getAccessToken(registeredService.getClientId(), Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            when(storedToken.isStateless()).thenReturn(true);
            val resolvedToken = getAccessToken(registeredService.getClientId(), Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            val registry = mock(TicketRegistry.class);
            when(registry.addTicket(any(Ticket.class))).thenReturn(storedToken);
            when(registry.getTicket(storedToken.getId(), OAuth20AccessToken.class)).thenReturn(resolvedToken);
            val context = spy(oidcConfigurationContext);
            when(context.getTicketRegistry()).thenReturn(registry);

            val controller = new OidcDynamicClientRegistrationEndpointController(context);
            val accessToken = controller.generateRegistrationAccessToken(new MockHttpServletRequest(),
                new MockHttpServletResponse(), registeredService, new OidcClientRegistrationRequest());
            assertSame(resolvedToken, accessToken);
            verify(registry).getTicket(storedToken.getId(), OAuth20AccessToken.class);
        }
    }
}
