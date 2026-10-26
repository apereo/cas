package org.apereo.cas.oidc.dynareg;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcClientIdMetadataDocumentProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientRegistrationRequestTranslator;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DefaultOidcClientIdMetadataDocumentResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDC")
class DefaultOidcClientIdMetadataDocumentResolverTests {

    private static String document(final String clientId, final String extra) {
        return """
            {"client_id":"%s","client_name":"Example MCP Client","client_uri":"https://app.example.org",
            "redirect_uris":["http://127.0.0.1:3000/callback","https://app.example.org/callback"],
            "grant_types":["authorization_code","refresh_token"],"response_types":["code"]%s}
            """.formatted(clientId, extra);
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.client-id-metadata-document.enabled=true")
    class EnabledTests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcClientRegistrationRequestTranslator")
        private OidcClientRegistrationRequestTranslator translator;

        @Autowired
        @Qualifier("clientRegistrationRequestSerializer")
        private StringSerializer<OidcClientRegistrationRequest> serializer;

        @Autowired
        @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
        private HttpClient httpClient;

        @Autowired
        @Qualifier(OidcClientIdMetadataDocumentResolver.BEAN_NAME)
        private OidcClientIdMetadataDocumentResolver oidcClientIdMetadataDocumentResolver;

        private DefaultOidcClientIdMetadataDocumentResolver resolver(final OidcClientIdMetadataDocumentProperties properties,
                                                                     final OidcClientIdMetadataDocumentFetcher fetcher) {
            return new DefaultOidcClientIdMetadataDocumentResolver(properties, servicesManager, translator, serializer, fetcher);
        }

        private static String newClientId() {
            return "https://app.example.org/" + UUID.randomUUID() + "/client.json";
        }

        @Test
        void verifyClientIsRegisteredFromDocument() throws Throwable {
            val clientId = newClientId();
            val fetches = new AtomicInteger();
            val resolver = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true), uri -> {
                fetches.incrementAndGet();
                return new OidcClientIdMetadataDocument(uri.toString(), document(uri.toString(), StringUtils.EMPTY), Duration.ofHours(1));
            });
            val service = resolver.resolve(clientId).orElseThrow();
            assertEquals(clientId, service.getClientId());
            assertEquals("Example MCP Client", service.getName());
            assertTrue(service.getClientSecrets().isEmpty());
            assertEquals("none", service.getTokenEndpointAuthenticationMethod());
            assertFalse(service.isBypassApprovalPrompt());
            assertTrue(service.isGenerateRefreshToken());
            assertEquals(Integer.MAX_VALUE, service.getEvaluationOrder());
            assertTrue(OidcClientIdMetadataDocumentResolver.isClientIdMetadataDocumentClient(service));
            assertTrue(service.matches("http://127.0.0.1:3000/callback"));
            assertTrue(service.matches("https://app.example.org/callback"));
            assertFalse(service.matches("https://app.example.org/callback/other"));
            assertFalse(service.matches("https://appXexample.org/callback"));
            assertTrue(service.getExpirationPolicy().isDeleteWhenExpired());
            assertNotNull(service.getExpirationPolicy().getExpirationDate());
            assertEquals(service.getId(), Objects.requireNonNull(
                OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId)).getId());

            assertTrue(resolver.resolve(clientId).isPresent());
            assertEquals(1, fetches.get());
        }

        @Test
        void verifyClientIsRefreshedWhenDue() throws Throwable {
            val clientId = newClientId();
            val names = new ArrayDeque<>(List.of("First", "Second"));
            val properties = new OidcClientIdMetadataDocumentProperties().setEnabled(true).setMinimumCacheDuration("PT0S");
            val failing = new AtomicBoolean();
            val resolver = resolver(properties, uri -> {
                if (failing.get()) {
                    throw new IOException("Document is unavailable");
                }
                val json = document(uri.toString(), StringUtils.EMPTY).replace("Example MCP Client", Objects.requireNonNull(names.poll()));
                return new OidcClientIdMetadataDocument(uri.toString(), json, Duration.ZERO);
            });
            val first = resolver.resolve(clientId).orElseThrow();
            assertEquals("First", first.getName());
            val second = resolver.resolve(clientId).orElseThrow();
            assertEquals(first.getId(), second.getId());
            assertEquals("Second", second.getName());
            failing.set(true);
            assertEquals("Second", resolver.resolve(clientId).orElseThrow().getName());
        }

        @Test
        void verifyCacheDurationIsBounded() {
            val resolver = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true)
                .setMinimumCacheDuration("PT5M").setMaximumCacheDuration("PT1H"), uri -> {
                throw new IllegalStateException();
            });
            assertEquals(Duration.ofMinutes(5), resolver.cacheDuration(new OidcClientIdMetadataDocument("x", "{}", Duration.ZERO)));
            assertEquals(Duration.ofMinutes(30), resolver.cacheDuration(new OidcClientIdMetadataDocument("x", "{}", Duration.ofMinutes(30))));
            assertEquals(Duration.ofHours(1), resolver.cacheDuration(new OidcClientIdMetadataDocument("x", "{}", Duration.ofDays(2))));
        }

        @Test
        void verifyRefusedDocuments() {
            val extras = List.of(
                ",\"client_secret\":\"secret\"",
                ",\"jwks_uri\":\"https://app.example.org/jwks\"",
                ",\"sector_identifier_uri\":\"https://app.example.org/sector\"",
                ",\"backchannel_logout_uri\":\"https://app.example.org/logout\"",
                ",\"token_endpoint_auth_method\":\"client_secret_basic\"",
                ",\"token_endpoint_auth_method\":\"private_key_jwt\"");
            for (val extra : extras) {
                val clientId = newClientId();
                val resolver = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true),
                    uri -> new OidcClientIdMetadataDocument(uri.toString(), document(uri.toString(), extra), Duration.ZERO));
                assertTrue(resolver.resolve(clientId).isEmpty(), extra);
            }
            val documents = List.of(
                document("https://other.example.org/client.json", StringUtils.EMPTY),
                document("%s", StringUtils.EMPTY).replace("\"grant_types\":[\"authorization_code\",\"refresh_token\"]", "\"grant_types\":[\"client_credentials\"]"),
                document("%s", StringUtils.EMPTY).replace("\"response_types\":[\"code\"]", "\"response_types\":[\"token\"]"),
                document("%s", StringUtils.EMPTY).replace("http://127.0.0.1:3000/callback", "http://app.example.org/callback"),
                document("%s", StringUtils.EMPTY).replace("https://app.example.org/callback", "https://app.example.org/callback#fragment"),
                document("%s", StringUtils.EMPTY).replace("\"client_name\":\"Example MCP Client\",", StringUtils.EMPTY));
            for (val json : documents) {
                val clientId = newClientId();
                val resolver = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true),
                    uri -> new OidcClientIdMetadataDocument(uri.toString(), json.replace("%s", uri.toString()), Duration.ZERO));
                assertTrue(resolver.resolve(clientId).isEmpty(), json);
            }
        }

        @Test
        void verifyRefusedClientIds() {
            val fetches = new AtomicInteger();
            val resolver = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true), uri -> {
                fetches.incrementAndGet();
                return new OidcClientIdMetadataDocument(uri.toString(), document(uri.toString(), StringUtils.EMPTY), Duration.ZERO);
            });
            for (val clientId : List.of("https://app.example.org", "https://user@app.example.org/client.json",
                "https://app.example.org/client.json?x=1", "https://app.example.org/client.json#x",
                "https://app.example.org/a/../client.json", "https://app.example.org/./client.json")) {
                assertTrue(resolver.resolve(clientId).isEmpty(), clientId);
            }
            assertTrue(resolver.resolve("http://app.example.org/client.json").isEmpty());
            assertTrue(resolver.resolve("client").isEmpty());
            assertTrue(resolver.resolve(null).isEmpty());
            assertEquals(0, fetches.get());
            assertTrue(resolver(new OidcClientIdMetadataDocumentProperties(), uri -> {
                throw new IllegalStateException();
            }).resolve(newClientId()).isEmpty());
        }

        @Test
        void verifyPrivateKeyJwtClient() throws Throwable {
            val clientId = newClientId();
            val jwks = ",\"token_endpoint_auth_method\":\"private_key_jwt\",\"jwks\":{\"keys\":[{\"kty\":\"EC\",\"crv\":\"P-256\","
                + "\"x\":\"f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU\",\"y\":\"x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0\",\"kid\":\"k1\"}]}";
            val resolver = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true),
                uri -> new OidcClientIdMetadataDocument(uri.toString(), document(uri.toString(), jwks), Duration.ZERO));
            val service = resolver.resolve(clientId).orElseThrow();
            assertEquals("private_key_jwt", service.getTokenEndpointAuthenticationMethod());
            assertTrue(service.getJwks().contains("k1"));
        }

        @Test
        void verifyClientLimitAndAdministratorClients() throws Throwable {
            val properties = new OidcClientIdMetadataDocumentProperties().setEnabled(true).setMaximumClients(0);
            val resolver = resolver(properties,
                uri -> new OidcClientIdMetadataDocument(uri.toString(), document(uri.toString(), StringUtils.EMPTY), Duration.ZERO));
            assertTrue(resolver.resolve(newClientId()).isEmpty());

            val clientId = newClientId();
            val registered = getOidcRegisteredService(clientId, "https://admin.example.org/.*");
            servicesManager.save(registered);
            val unlimited = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true), uri -> {
                throw new IllegalStateException("Administrator clients are not fetched");
            });
            val found = unlimited.resolve(clientId).orElseThrow();
            assertEquals(registered.getId(), found.getId());
            assertFalse(OidcClientIdMetadataDocumentResolver.isClientIdMetadataDocumentClient(found));
        }

        @Test
        void verifyDocumentIsFetched() throws Throwable {
            try (val webServer = new MockWebServer(false, "{}")) {
                webServer.start();
                val clientId = newClientId();
                val location = URI.create("http://localhost:%s/client.json".formatted(webServer.getPort()));
                webServer.responseBody(document(clientId, StringUtils.EMPTY));
                webServer.headers(Map.of("Cache-Control", "public, max-age=600"));
                val loopback = InetAddress.getByName("127.0.0.1");
                val fetcher = new DefaultOidcClientIdMetadataDocumentFetcher(httpClient, Duration.ofSeconds(5),
                    _ -> true, _ -> new InetAddress[]{loopback});
                val fetched = fetcher.fetch(location);
                assertEquals(location.toString(), fetched.clientId());
                assertEquals(Duration.ofMinutes(10), fetched.cacheLifetime());
                assertTrue(fetched.json().contains("Example MCP Client"));

                val resolver = resolver(new OidcClientIdMetadataDocumentProperties().setEnabled(true), uri -> {
                    val document = fetcher.fetch(location);
                    return new OidcClientIdMetadataDocument(uri.toString(), document.json(), document.cacheLifetime());
                });
                val service = resolver.resolve(clientId).orElseThrow();
                assertEquals(clientId, service.getClientId());
                val refresh = service.getProperties().get(RegisteredServiceProperties.OIDC_CLIENT_ID_METADATA_DOCUMENT_REFRESH.getPropertyName());
                assertNotNull(refresh);
                assertTrue(Instant.parse(refresh.value()).isAfter(Instant.now(Clock.systemUTC()).plus(Duration.ofMinutes(9))));

                webServer.headers(Map.of("Cache-Control", "no-store"));
                assertEquals(Duration.ZERO, fetcher.fetch(location).cacheLifetime());
                webServer.responseStatus(org.springframework.http.HttpStatus.FOUND);
                assertThrows(IllegalArgumentException.class, () -> fetcher.fetch(location));
                webServer.responseStatus(org.springframework.http.HttpStatus.OK);
                webServer.setContentType("text/html");
                assertThrows(IllegalArgumentException.class, () -> fetcher.fetch(location));
                webServer.setContentType("application/json");
                webServer.responseBody("x".repeat(DefaultOidcClientIdMetadataDocumentFetcher.MAXIMUM_DOCUMENT_SIZE + 1));
                assertThrows(IllegalArgumentException.class, () -> fetcher.fetch(location));
            }
        }

        @Test
        void verifyNonPublicAddressesAreRefused() throws Throwable {
            for (val address : List.of("127.0.0.1", "10.1.2.3", "172.16.0.1", "192.168.1.1", "169.254.169.254", "100.64.0.1",
                "0.0.0.0", "224.0.0.1", "255.255.255.255", "192.0.2.1", "198.18.0.1", "::1", "::", "fe80::1", "fc00::1",
                "fd12:3456::1", "2001:db8::1", "::ffff:127.0.0.1", "::ffff:10.0.0.1", "64:ff9b::a00:1")) {
                assertFalse(DefaultOidcClientIdMetadataDocumentFetcher.isPublicAddress(InetAddress.getByName(address)), address);
            }
            for (val address : List.of("93.184.216.34", "8.8.8.8", "2606:4700::6810:84e5", "64:ff9b::808:808")) {
                assertTrue(DefaultOidcClientIdMetadataDocumentFetcher.isPublicAddress(InetAddress.getByName(address)), address);
            }
            val publicAddress = InetAddress.getByAddress(new byte[]{93, (byte) 184, (byte) 216, 34});
            val fetcher = new DefaultOidcClientIdMetadataDocumentFetcher(httpClient, Duration.ofSeconds(1),
                DefaultOidcClientIdMetadataDocumentFetcher::isPublicAddress,
                _ -> new InetAddress[]{publicAddress, InetAddress.getLoopbackAddress()});
            assertThrows(IllegalArgumentException.class, () -> fetcher.fetch(URI.create("https://app.example.org/client.json")));
            val unresolved = new DefaultOidcClientIdMetadataDocumentFetcher(httpClient, Duration.ofSeconds(1),
                DefaultOidcClientIdMetadataDocumentFetcher::isPublicAddress, _ -> new InetAddress[0]);
            assertThrows(IllegalArgumentException.class, () -> unresolved.fetch(URI.create("https://app.example.org/client.json")));
        }

        @Test
        void verifyInterceptorRefusesLoopbackDocuments() throws Throwable {
            try (val webServer = new MockWebServer(true, "{}")) {
                webServer.start();
                val clientId = "https://localhost:%s/client.json".formatted(webServer.getPort());
                webServer.responseBody(document(clientId, StringUtils.EMPTY));
                val request = new MockHttpServletRequest("GET", "/cas/oidc/" + OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL);
                request.addParameter(OAuth20Constants.CLIENT_ID, clientId);
                oauthInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());
                assertEquals(0, webServer.getRequestCount());
                assertNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId));
                assertTrue(oidcClientIdMetadataDocumentResolver.resolve(clientId).isEmpty());
            }
        }

        @Test
        void verifyDiscoveryAdvertisesSupport() throws Throwable {
            mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client_id_metadata_document_supported").value(true));
        }
    }
}
