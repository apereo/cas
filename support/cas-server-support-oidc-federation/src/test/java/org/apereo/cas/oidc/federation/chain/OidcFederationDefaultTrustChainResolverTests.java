package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustAnchorProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationDefaultTrustMarkValidator;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientRegistrationUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyRevocation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.policy.MetadataPolicy;
import com.nimbusds.openid.connect.sdk.federation.policy.language.PolicyViolationException;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.ResolveException;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainSet;
import com.nimbusds.openid.connect.sdk.federation.trust.constraints.TrustChainConstraints;
import com.nimbusds.openid.connect.sdk.rp.ApplicationType;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import lombok.val;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcFederationDefaultTrustChainResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDCWeb")
class OidcFederationDefaultTrustChainResolverTests {
    /**
     * Historical keys and trust mark status are only requested over {@code https}; the test servers present a
     * certificate of their own, which only this client trusts.
     */
    private static final OidcFederationHttpClient TEST_HTTP_CLIENT =
        new OidcFederationHttpClient(new OidcFederationProperties(), CasSSLContext.disabled());

    abstract static class BaseTests extends AbstractOidcTrustAnchorFederationTests {
        @Autowired
        @Qualifier(OidcFederationTrustChainResolver.BEAN_NAME)
        protected OidcFederationTrustChainResolver oidcFederationTrustChainResolver;
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Autowired
        @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
        private OidcFederationEntityStatementService entityStatementService;

        @Test
        void verifyAuthorityHintsAreBounded() throws Exception {
            val entityId = "https://rp.example.org";
            val authorityHints = IntStream.range(0, 6).mapToObj(i -> new EntityID("https://superior" + i + ".example.org")).toList();
            val entityConfiguration = entityStatementService.createAndSign(entityId, entityId, new JSONObject(), null, authorityHints);
            val delegate = mock(EntityStatementRetriever.class);
            when(delegate.fetchEntityConfiguration(any())).thenReturn(entityConfiguration);

            assertThrows(ResolveException.class, () -> new OidcFederationBoundedEntityStatementRetriever(delegate, 5)
                .fetchEntityConfiguration(new EntityID(entityId)));
            assertEquals(entityConfiguration, new OidcFederationBoundedEntityStatementRetriever(delegate, 6)
                .fetchEntityConfiguration(new EntityID(entityId)));

            val properties = new OidcFederationProperties()
                .setRole(OidcFederationRole.OPENID_PROVIDER)
                .setMaxPathLength(1)
                .setAuthorityHints(List.of("https://ta.example.org"));
            val resolver = OidcFederationDefaultTrustChainResolver.buildTrustChainResolvers(properties).getFirst();
            assertEquals(1, resolver.getConstraints().getMaxPathLength());
            assertInstanceOf(OidcFederationBoundedEntityStatementRetriever.class, resolver.getEntityStatementRetriever());
        }

        @Test
        void verifyNoTrust() throws Exception {
            assertTrue(oidcFederationTrustChainResolver.resolveTrustChains(UUID.randomUUID().toString()).isEmpty());
            assertTrue(oidcFederationTrustChainResolver.resolveTrustChains("https://rp.example.com").isEmpty());
            assertTrue(oidcFederationTrustChainResolver.resolveTrustChains("http://rp.example.com").isEmpty());
        }

        @Test
        void verifyTrustAnchorsWithKeysObtainedOutOfBand() throws Exception {
            val inlineKey = new RSAKeyGenerator(2048).keyID("inline").generate();
            val fileKey = new RSAKeyGenerator(2048).keyID("file").generate();
            val file = Files.createTempFile("trust-anchor", ".jwks");
            Files.writeString(file, new JWKSet(fileKey).toString(false));

            val properties = new OidcFederationProperties()
                .setRole(OidcFederationRole.OPENID_PROVIDER)
                .setAuthorityHints(List.of("https://intermediate.example.org"));
            properties.getTrustAnchors().add(new OidcFederationTrustAnchorProperties()
                .setEntityId("https://ta1.example.org").setJwks(new JWKSet(inlineKey).toString(false)));
            properties.getTrustAnchors().add(new OidcFederationTrustAnchorProperties()
                .setEntityId("https://ta2.example.org").setJwks("file:" + file));
            properties.getTrustAnchors().add(new OidcFederationTrustAnchorProperties().setEntityId("https://ta3.example.org"));

            val resolvers = OidcFederationDefaultTrustChainResolver.buildTrustChainResolvers(properties);
            assertEquals(3, resolvers.size());
            val inlineKeys = resolvers.getFirst().getTrustAnchors().get(new EntityID("https://ta1.example.org"));
            assertEquals("inline", inlineKeys.getKeys().getFirst().getKeyID());
            assertFalse(inlineKeys.getKeys().getFirst().isPrivate());
            val fileKeys = resolvers.get(1).getTrustAnchors().get(new EntityID("https://ta2.example.org"));
            assertEquals("file", fileKeys.getKeys().getFirst().getKeyID());
            assertFalse(fileKeys.getKeys().getFirst().isPrivate());
            assertTrue(resolvers.get(2).getTrustAnchors().containsKey(new EntityID("https://ta3.example.org")));
            assertTrue(resolvers.stream().noneMatch(resolver ->
                resolver.getTrustAnchors().containsKey(new EntityID("https://intermediate.example.org"))));

            val fallback = OidcFederationDefaultTrustChainResolver.buildTrustChainResolvers(new OidcFederationProperties()
                .setRole(OidcFederationRole.OPENID_PROVIDER)
                .setAuthorityHints(List.of("https://ta.example.org")));
            assertEquals(1, fallback.size());
            assertTrue(fallback.getFirst().getTrustAnchors().containsKey(new EntityID("https://ta.example.org")));
        }

        @Test
        void verifyOnlyAsymmetricClientAuthentication() throws Exception {
            val resolver = (OidcFederationDefaultTrustChainResolver) oidcFederationTrustChainResolver;
            val entityId = new EntityID("https://rp.example.com");
            val expiration = Date.from(ZonedDateTime.now(Clock.systemUTC()).plusDays(1).toInstant());
            val clientMetadata = new OIDCClientMetadata();
            clientMetadata.setScope(new Scope(OidcConstants.StandardScopes.OPENID.getScope()));
            clientMetadata.setApplicationType(ApplicationType.WEB);
            clientMetadata.setRedirectionURI(new URI("https://rp.example.com/callback"));

            val defaultService = resolver.extractRegisteredService(entityId, clientMetadata, expiration);
            assertNotNull(defaultService);
            assertEquals(ClientAuthenticationMethod.PRIVATE_KEY_JWT.getValue(), defaultService.getTokenEndpointAuthenticationMethod());

            clientMetadata.setTokenEndpointAuthMethod(ClientAuthenticationMethod.TLS_CLIENT_AUTH);
            val tlsService = resolver.extractRegisteredService(entityId, clientMetadata, expiration);
            assertNotNull(tlsService);
            assertEquals(ClientAuthenticationMethod.TLS_CLIENT_AUTH.getValue(), tlsService.getTokenEndpointAuthenticationMethod());

            for (val method : List.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, ClientAuthenticationMethod.NONE)) {
                clientMetadata.setTokenEndpointAuthMethod(method);
                assertNull(resolver.extractRegisteredService(entityId, clientMetadata, expiration));
            }
        }

        @Test
        void verifyOnlyHttpsKeySetLocations() throws Exception {
            val resolver = (OidcFederationDefaultTrustChainResolver) oidcFederationTrustChainResolver;
            val entityId = new EntityID("https://rp.example.com");
            val expiration = Date.from(ZonedDateTime.now(Clock.systemUTC()).plusDays(1).toInstant());
            val clientMetadata = new OIDCClientMetadata();
            clientMetadata.setScope(new Scope(OidcConstants.StandardScopes.OPENID.getScope()));
            clientMetadata.setApplicationType(ApplicationType.WEB);
            clientMetadata.setRedirectionURI(new URI("https://rp.example.com/callback"));

            clientMetadata.setJWKSetURI(new URI("https://rp.example.com/jwks.json"));
            val service = resolver.extractRegisteredService(entityId, clientMetadata, expiration);
            assertNotNull(service);
            assertEquals("https://rp.example.com/jwks.json", service.getJwks());

            for (val location : List.of("file:///etc/hosts", "http://rp.example.com/jwks.json", "jar:file:/tmp/keys.jar!/jwks.json")) {
                clientMetadata.setJWKSetURI(new URI(location));
                assertNull(resolver.extractRegisteredService(entityId, clientMetadata, expiration));
            }
        }
    }

    @Nested
    @Import(MockTrustTests.MockTrustTestConfiguration.class)
    class MockTrustTests extends BaseTests {
        private static final String INLINE_JWKS_JSON = """
            {
              "keys":[
                {
                  "kty":"RSA",
                  "e":"AQAB",
                  "use":"sig",
                  "kid":"defaultjwks0726",
                  "n":"2moVQ...2aq7Q"
                }
              ]
            }
            """;

        @Test
        void verifyResolved() throws Exception {
            val registeredService = oidcFederationTrustChainResolver.resolveTrustChains("https://rp.example.com").orElseThrow();
            assertNotNull(registeredService);
            assertNotNull(registeredService.getExpirationPolicy());
            assertFalse(registeredService.getExpirationPolicy().isExpired());
            assertTrue(registeredService.getExpirationPolicy().isDeleteWhenExpired());
            assertTrue(registeredService.getProperties().containsKey(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE));
            assertTrue(registeredService.getProperties()
                .get(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE)
                .getBooleanValue());
            assertTrue(registeredService.matches("https://example.com?parameter=true"));
            assertTrue(registeredService.matches("https://example2.com?parameter=false"));
            assertFalse(registeredService.matches("https://exampleXcom?parameter=true"));
            assertFalse(registeredService.matches("https://EXAMPLE.com?parameter=TRUE"));
            assertEquals(List.of("https://example.com?parameter=true", "https://example2.com?parameter=false"),
                OidcClientRegistrationUtils.toRedirectUris(registeredService.getServiceId()));
        }

        @Test
        void verifyInlineJwksIsMappedWhenJwksUriIsMissing() throws Exception {
            val registeredService = oidcFederationTrustChainResolver.resolveTrustChains("https://rp.example.com").orElseThrow();
            assertNotNull(registeredService);
            assertEquals(JWKSet.parse(INLINE_JWKS_JSON).toString(), registeredService.getJwks());
        }

        @TestConfiguration(value = "MockTrustTestConfiguration", proxyBeanMethods = false)
        static class MockTrustTestConfiguration {
            @Bean
            public TrustChainResolver mockTrustChainResolver(
                @Qualifier(OidcFederationEntityStatementService.BEAN_NAME)
                final OidcFederationEntityStatementService oidcFederationEntityStatementService,
                final CasConfigurationProperties casProperties)
                    throws Exception {

                val issuer = casProperties.getAuthn().getOidc().getCore().getIssuer();
                val metadata = new JSONObject();
                val authorityHints = casProperties.getAuthn().getOidc()
                        .getFederation().getAuthorityHints().stream().map(EntityID::new).toList();

                val resolver = mock(TrustChainResolver.class);
                val trustChain = mock(TrustChain.class);
                val trustChainSet = new TrustChainSet();
                trustChainSet.add(trustChain);
                val metadataPolicy = mock(MetadataPolicy.class);

                val clientMetadata = new OIDCClientMetadata();
                clientMetadata.setScope(new Scope(OidcConstants.StandardScopes.OPENID.getScope()));
                clientMetadata.setApplicationType(ApplicationType.WEB);
                clientMetadata.setSubjectType(SubjectType.PUBLIC);
                clientMetadata.setEmailContacts(List.of("cas@example.org"));
                clientMetadata.setRedirectionURIs(Set.of(new URI("https://example.com?parameter=true"),
                        new URI("https://example2.com?parameter=false")));
                clientMetadata.setPostLogoutRedirectionURIs(Set.of(new URI("https://logout.example.com")));
                clientMetadata.setGrantTypes(Set.of(GrantType.AUTHORIZATION_CODE));
                clientMetadata.setResponseTypes(Set.of(ResponseType.CODE));
                clientMetadata.setJWKSet(JWKSet.parse(INLINE_JWKS_JSON));

                val rpMetadata = clientMetadata.toJSONObject(true);

                metadata.put(EntityType.OPENID_RELYING_PARTY.getValue(), rpMetadata);

                when(metadataPolicy.apply(any())).thenReturn(rpMetadata);
                when(trustChain.resolveCombinedMetadataPolicy(any(EntityType.class))).thenReturn(metadataPolicy);
                when(trustChain.resolveExpirationTime()).thenReturn(Date.from(ZonedDateTime.now(Clock.systemUTC()).plusDays(7).toInstant()));

                val leafConfiguration = oidcFederationEntityStatementService.createAndSign(issuer, issuer,
                        metadata, null, authorityHints);
                when(trustChain.getLeafConfiguration()).thenReturn(leafConfiguration);
                when(resolver.resolveTrustChains(any(EntityID.class))).thenReturn(trustChainSet);

                val trustAnchorConfiguration = oidcFederationEntityStatementService.createAndSign(issuer, issuer,
                        new JSONObject(), null, null);
                val retriever = mock(EntityStatementRetriever.class);
                when(retriever.fetchEntityConfiguration(any())).thenReturn(trustAnchorConfiguration);
                when(resolver.getEntityStatementRetriever()).thenReturn(retriever);
                when(trustChain.getTrustAnchorEntityID()).thenReturn(new EntityID(issuer));
                when(trustChain.getSuperiorStatements()).thenReturn(List.of(trustAnchorConfiguration));
                return resolver;
            }
        }
    }

    /**
     * Resolutions are bounded (OpenID Federation 1.0, section 18.1): each sends at most a number of requests, reusing the
     * statements it already fetched, and at most a number of them run at the same time.
     */
    @Nested
    class ResolutionLimitTests {
        private static final String RELYING_PARTY = "https://rp.example.org";

        private static final String TRUST_ANCHOR = "https://ta.example.org";

        private final RSAKey entityKey = generateKey("entity");

        @Test
        void verifyRequestsPerResolutionAreBounded() throws Exception {
            val properties = new OidcFederationProperties().setMaxRequestsPerResolution(3).setMaxAuthorityHints(5);
            val retriever = mock(EntityStatementRetriever.class);
            val calls = new AtomicInteger();
            when(retriever.fetchEntityConfiguration(any())).thenAnswer(invocation -> {
                calls.incrementAndGet();
                val target = invocation.getArgument(0, EntityID.class).getValue();
                return entityConfiguration(target, IntStream.range(0, 5).mapToObj(index -> target + '/' + index).toList());
            });
            val resolver = new OidcFederationDefaultTrustChainResolver(List.of(OidcFederationDefaultTrustChainResolver.buildTrustChainResolver(
                    new EntityID(TRUST_ANCHOR), null, properties, new OidcFederationBudgetedEntityStatementRetriever(retriever))),
                new OidcFederationDefaultTrustMarkValidator(properties, TEST_HTTP_CLIENT), new OidcFederationResolutionLimiter(properties));
            assertTrue(resolver.resolveTrustChains(RELYING_PARTY).isEmpty());
            assertTrue(calls.get() <= 3, () -> calls.get() + " requests");
        }

        /**
         * A step of a resolution cannot tell only when one of its own requests ran out of time or found no room, or found
         * the trust anchor unreachable; a request that did so in an earlier step, or a budget used up, does not make a
         * later step unavailable.
         */
        @Test
        void verifyIndeterminateStepsAreTrackedPerStep() throws Exception {
            new OidcFederationResolutionLimiter(1, 1, Duration.ofSeconds(1)).execute(() -> {
                val before = OidcFederationResolutionLimiter.indeterminateMark();
                OidcFederationResolutionLimiter.currentBudget().orElseThrow().markIndeterminate();
                val after = OidcFederationResolutionLimiter.indeterminateMark();
                assertTrue(OidcFederationResolutionLimiter.isIndeterminateSince(before));
                assertFalse(OidcFederationResolutionLimiter.isIndeterminateSince(after));
                val trustAnchor = new EntityID(TRUST_ANCHOR);
                val beforeUnreachable = OidcFederationResolutionLimiter.unreachableMark(trustAnchor);
                OidcFederationResolutionLimiter.currentBudget().orElseThrow().markUnreachable(TRUST_ANCHOR);
                val afterUnreachable = OidcFederationResolutionLimiter.unreachableMark(trustAnchor);
                assertTrue(OidcFederationResolutionLimiter.isUnreachableSince(trustAnchor, beforeUnreachable));
                assertFalse(OidcFederationResolutionLimiter.isUnreachableSince(trustAnchor, afterUnreachable));
                assertFalse(OidcFederationResolutionLimiter.isUnreachableSince(new EntityID(RELYING_PARTY), 0));
                OidcFederationResolutionLimiter.consumeRequest("first");
                assertThrows(IllegalStateException.class, () -> OidcFederationResolutionLimiter.consumeRequest("second"));
                assertFalse(OidcFederationResolutionLimiter.isIndeterminateSince(after));
                return null;
            });
            assertFalse(OidcFederationResolutionLimiter.isIndeterminateSince(0));
        }

        @Test
        void verifyStatementsAreFetchedOncePerResolution() throws Exception {
            val retriever = mock(EntityStatementRetriever.class);
            when(retriever.fetchEntityConfiguration(any())).thenReturn(entityConfiguration(RELYING_PARTY, List.of()));
            val budgeted = new OidcFederationBudgetedEntityStatementRetriever(retriever);
            new OidcFederationResolutionLimiter(1, 2, Duration.ofSeconds(1)).execute(() -> {
                budgeted.fetchEntityConfiguration(new EntityID(RELYING_PARTY));
                budgeted.fetchEntityConfiguration(new EntityID(RELYING_PARTY));
                budgeted.fetchEntityConfiguration(new EntityID(TRUST_ANCHOR));
                assertThrows(ResolveException.class, () -> budgeted.fetchEntityConfiguration(new EntityID("https://other.example.org")));
                assertThrows(IllegalStateException.class, () -> OidcFederationResolutionLimiter.consumeRequest("trust mark status"));
                return null;
            });
            verify(retriever, times(2)).fetchEntityConfiguration(any());
            budgeted.fetchEntityConfiguration(new EntityID("https://other.example.org"));
            OidcFederationResolutionLimiter.consumeRequest("trust mark status");
            verify(retriever, times(3)).fetchEntityConfiguration(any());
        }

        @Test
        void verifySimultaneousResolutionsAreBounded() throws Exception {
            val limiter = new OidcFederationResolutionLimiter(1, 10, Duration.ofMillis(200));
            val started = new CountDownLatch(1);
            val release = new CountDownLatch(1);
            try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
                val running = executor.submit(() -> limiter.execute(() -> {
                    started.countDown();
                    release.await();
                    return "first";
                }));
                started.await();
                assertThrows(OidcFederationResolutionBusyException.class, () -> limiter.execute(() -> "second"));
                release.countDown();
                assertEquals("first", running.get());
            }
            assertEquals("nested", limiter.execute(() -> limiter.execute(() -> "nested")));
        }

        /**
         * Every request of a resolution must be done before the resolution runs out of time, and its response may not
         * exceed a fixed size; an entity that cannot be reached is remembered by the resolution, while one that the
         * resolution ran out of time for is not.
         */
        @Test
        void verifyRequestsAreBoundedInTimeAndSize() throws Exception {
            val large = "x".repeat(OidcFederationHttpClient.MAX_RESPONSE_BYTES + 1);
            try (val webServer = new MockWebServer(true, new ByteArrayResource(large.getBytes(StandardCharsets.UTF_8)))) {
                webServer.start();
                val request = new HTTPRequest(HTTPRequest.Method.GET, new URI("https://localhost:%s/large".formatted(webServer.getPort())));
                val exception = assertThrows(IOException.class, () -> TEST_HTTP_CLIENT.send(request));
                assertTrue(exception.getMessage().contains("larger than"), exception::getMessage);
                assertThrows(SocketTimeoutException.class, () -> new OidcFederationResolutionLimiter(1, 10, Duration.ofSeconds(1), Duration.ZERO)
                    .execute(() -> TEST_HTTP_CLIENT.send(request)));
            }
            val retriever = new OidcFederationHttpEntityStatementRetriever(TEST_HTTP_CLIENT);
            val unreachable = new EntityID("https://localhost:1");
            new OidcFederationResolutionLimiter(new OidcFederationProperties()).execute(() -> {
                assertThrows(ResolveException.class, () -> retriever.fetchEntityConfiguration(unreachable));
                assertTrue(OidcFederationResolutionLimiter.currentBudget().orElseThrow().getUnreachableCount(unreachable.getValue()) > 0);
                return null;
            });
            new OidcFederationResolutionLimiter(1, 10, Duration.ofSeconds(1), Duration.ZERO).execute(() -> {
                assertThrows(ResolveException.class, () -> retriever.fetchEntityConfiguration(unreachable));
                assertEquals(0, OidcFederationResolutionLimiter.currentBudget().orElseThrow().getUnreachableCount(unreachable.getValue()));
                return null;
            });
        }

        /**
         * An entity that answers with a server error is remembered as unreachable, as one that cannot be reached is, so
         * that a trust anchor in that state makes the resolution unavailable rather than failed; one that answers that
         * the statement is not found is not.
         */
        @Test
        void verifyServerErrorsAreUnreachable() throws Exception {
            try (val webServer = new MockWebServer(true, new ByteArrayResource("{}".getBytes(StandardCharsets.UTF_8)))) {
                webServer.start();
                val retriever = new OidcFederationHttpEntityStatementRetriever(TEST_HTTP_CLIENT);
                val entityId = new EntityID("https://localhost:%s".formatted(webServer.getPort()));
                webServer.responseStatus(HttpStatus.SERVICE_UNAVAILABLE);
                new OidcFederationResolutionLimiter(new OidcFederationProperties()).execute(() -> {
                    assertThrows(ResolveException.class, () -> retriever.fetchEntityConfiguration(entityId));
                    assertThrows(ResolveException.class, () -> retriever.fetchEntityStatement(
                        new URI(entityId.getValue() + "/fetch"), entityId, new EntityID(RELYING_PARTY)));
                    assertTrue(OidcFederationResolutionLimiter.currentBudget().orElseThrow().getUnreachableCount(entityId.getValue()) > 0);
                    return null;
                });
                webServer.responseStatus(HttpStatus.NOT_FOUND);
                new OidcFederationResolutionLimiter(new OidcFederationProperties()).execute(() -> {
                    assertThrows(ResolveException.class, () -> retriever.fetchEntityConfiguration(entityId));
                    assertEquals(0, OidcFederationResolutionLimiter.currentBudget().orElseThrow().getUnreachableCount(entityId.getValue()));
                    return null;
                });
            }
        }

        /**
         * A host that never answers is abandoned at the deadline and its connection closed: given the time a request
         * normally has, it is remembered as unreachable, as a trust anchor that hangs must be; given less, because the
         * resolution used up its time, it is not, since that tells nothing about the host, and the resolution cannot tell.
         */
        @Test
        void verifySilentHostIsAbandonedAtTheDeadline() throws Exception {
            val retriever = new OidcFederationHttpEntityStatementRetriever(TEST_HTTP_CLIENT);
            try (val silent = new ServerSocket(0); val executor = Executors.newVirtualThreadPerTaskExecutor()) {
                silent.setSoTimeout(5_000);
                val entityId = new EntityID("https://localhost:%s".formatted(silent.getLocalPort()));
                val closedByClient = executor.submit(() -> {
                    try (val socket = silent.accept()) {
                        socket.setSoTimeout(5_000);
                        socket.getInputStream().transferTo(OutputStream.nullOutputStream());
                        return Boolean.TRUE;
                    }
                });
                val started = Instant.now(Clock.systemUTC());
                new OidcFederationResolutionLimiter(1, 10, Duration.ofSeconds(1), Duration.ofMillis(1500)).execute(() -> {
                    val failure = assertThrows(ResolveException.class, () -> retriever.fetchEntityConfiguration(entityId));
                    assertInstanceOf(OidcFederationHttpClient.OutOfTimeException.class, failure.getCause());
                    assertEquals(0, OidcFederationResolutionLimiter.currentBudget().orElseThrow().getUnreachableCount(entityId.getValue()));
                    assertTrue(OidcFederationResolutionLimiter.isIndeterminateSince(0));
                    return null;
                });
                assertTrue(Duration.between(started, Instant.now(Clock.systemUTC())).toMillis() < 3_000);
                assertTrue(closedByClient.get(3, TimeUnit.SECONDS), "The connection is closed once the request is abandoned");
                new OidcFederationResolutionLimiter(new OidcFederationProperties()).execute(() -> {
                    assertThrows(ResolveException.class, () -> retriever.fetchEntityConfiguration(entityId));
                    assertTrue(OidcFederationResolutionLimiter.currentBudget().orElseThrow().getUnreachableCount(entityId.getValue()) > 0);
                    return null;
                });
            }
        }

        /**
         * Federation requests are only sent over {@code https}, and never to link-local, unspecified or multicast
         * addresses, including those embedded in IPv4-compatible and NAT64 IPv6 addresses.
         */
        @Test
        void verifyRequestTargetsAreChecked() throws Exception {
            for (val url : List.of("http://localhost:1/entity", "https://169.254.169.254/latest", "https://[::ffff:169.254.169.254]/latest",
                "https://[64:ff9b::a9fe:a9fe]/latest", "https://0.0.0.0/entity")) {
                assertThrows(OidcFederationHttpClient.RefusedRequestException.class,
                    () -> TEST_HTTP_CLIENT.send(new HTTPRequest(HTTPRequest.Method.GET, new URI(url))), url);
            }
        }

        /**
         * A relying party that asks for pairwise subject identifiers must have a sector of its own: its redirect URIs on
         * one host, read as hosts are when identifiers are calculated, or a sector identifier document that lists them
         * all. A document that cannot be had now leaves the sector unknown rather than refused.
         */
        @Test
        void verifyPairwiseSectorIsVerified() throws Exception {
            val properties = new OidcFederationProperties();
            val resolver = new OidcFederationDefaultTrustChainResolver(List.of(), new OidcFederationDefaultTrustMarkValidator(properties, TEST_HTTP_CLIENT),
                new OidcFederationResolutionLimiter(properties), new OidcFederationTrustAnchorKeys(TEST_HTTP_CLIENT), TEST_HTTP_CLIENT);
            val entityId = new EntityID(RELYING_PARTY);
            val metadata = new OIDCClientMetadata();
            metadata.setSubjectType(SubjectType.PAIRWISE);
            metadata.setRedirectionURI(new URI(RELYING_PARTY + "/callback"));
            assertTrue(resolver.hasVerifiedSector(entityId, metadata));
            for (val other : List.of("https://other.example.org/callback", "https://b\u00e9.example.org/callback", "https:///callback",
                "https:/other.example.org/callback", "com.example.app:/callback", "com.example.app://rp.example.org/callback")) {
                metadata.setRedirectionURIs(new LinkedHashSet<>(List.of(new URI(RELYING_PARTY + "/callback"), new URI(other))));
                assertFalse(resolver.hasVerifiedSector(entityId, metadata), other);
            }
            metadata.setRedirectionURIs(new LinkedHashSet<>(List.of(new URI(RELYING_PARTY + "/callback"), new URI("https://other.example.org/callback"))));
            try (val webServer = new MockWebServer(true)) {
                webServer.start();
                metadata.setSectorIDURI(new URI("https://localhost:%s/sector".formatted(webServer.getPort())));
                webServer.responseBodyJson(List.of(RELYING_PARTY + "/callback", "https://other.example.org/callback"));
                assertTrue(resolver.hasVerifiedSector(entityId, metadata));
                webServer.responseBodyJson(List.of(RELYING_PARTY + "/callback"));
                assertFalse(resolver.hasVerifiedSector(entityId, metadata));
                webServer.responseStatus(org.springframework.http.HttpStatus.NOT_FOUND);
                assertFalse(resolver.hasVerifiedSector(entityId, metadata));
                webServer.responseStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE);
                assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolver.hasVerifiedSector(entityId, metadata));
            }
            metadata.setSectorIDURI(new URI("https://localhost:1/sector"));
            assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolver.hasVerifiedSector(entityId, metadata));
            metadata.setSectorIDURI(new URI("https://169.254.169.254/sector"));
            assertFalse(resolver.hasVerifiedSector(entityId, metadata));
            metadata.setSectorIDURI(null);
            metadata.setRedirectionURI(new URI("com.example.app://rp.example.org/callback"));
            assertTrue(resolver.hasVerifiedSector(entityId, metadata));
        }

        /**
         * A trust anchor whose registration of the relying party cannot be built now, as when its sector identifier
         * document cannot be read, leaves the next trust anchor to try.
         */
        @Test
        void verifyNextTrustAnchorIsTriedWhenRegistrationIsUnavailable() throws Exception {
            val properties = new OidcFederationProperties();
            val metadata = new OIDCClientMetadata();
            metadata.setScope(new Scope(OidcConstants.StandardScopes.OPENID.getScope()));
            metadata.setRedirectionURI(new URI(RELYING_PARTY + "/callback"));
            val attempts = new AtomicInteger();
            val resolver = new OidcFederationDefaultTrustChainResolver(List.of(mock(TrustChainResolver.class), mock(TrustChainResolver.class)),
                new OidcFederationDefaultTrustMarkValidator(properties, TEST_HTTP_CLIENT), new OidcFederationResolutionLimiter(properties)) {
                @Override
                protected Optional<TrustChainMetadata> fetchRelyingPartyMetadata(final TrustChainResolver trustChainResolver, final EntityID entityId) {
                    return Optional.of(new TrustChainMetadata(metadata, Date.from(Instant.now(Clock.systemUTC()).plusSeconds(3600)), new Date()));
                }

                @Override
                protected OidcRegisteredService extractRegisteredService(final EntityID entityId, final OIDCClientMetadata rpMetadata,
                                                                         final Date expirationTime) {
                    if (attempts.incrementAndGet() == 1) {
                        throw new OidcFederationResolutionUnavailableException("Sector identifier URI cannot be read now");
                    }
                    return super.extractRegisteredService(entityId, rpMetadata, expirationTime);
                }
            };
            assertTrue(resolver.resolveTrustChains(RELYING_PARTY).isPresent());
            assertEquals(2, attempts.get());
        }

        private EntityStatement entityConfiguration(final String entityId, final List<String> authorityHints) throws Exception {
            val now = Instant.now(Clock.systemUTC());
            val claims = new EntityStatementClaimsSet(new EntityID(entityId), new EntityID(entityId),
                Date.from(now.minusSeconds(60)), Date.from(now.plusSeconds(3600)), new JWKSet(entityKey.toPublicJWK()));
            if (!authorityHints.isEmpty()) {
                claims.setAuthorityHints(authorityHints.stream().map(EntityID::new).toList());
            }
            return EntityStatement.sign(claims, entityKey);
        }

        private static RSAKey generateKey(final String keyId) {
            return FunctionUtils.doUnchecked(() -> new RSAKeyGenerator(2048).keyID(keyId).generate());
        }
    }

    /**
     * A trust chain the relying party sends, such as in the {@code trust_chain} header of its request object, is used
     * once verified, without resolving; a chain that cannot be used has the trust chains resolved instead, which the
     * retriever of these tests refuses.
     */
    @Nested
    class SentTrustChainTests {
        private static final String TRUST_ANCHOR = "https://ta.example.org";

        private static final String INTERMEDIATE = "https://intermediate.example.org";

        private static final String RELYING_PARTY = "https://rp.example.org";

        private final RSAKey trustAnchorKey = generateKey("ta");

        private final RSAKey intermediateKey = generateKey("intermediate");

        private final RSAKey relyingPartyKey = generateKey("rp");

        private final EntityStatementRetriever retriever = mock(EntityStatementRetriever.class);

        @Test
        void verifySentTrustChainIsUsedWithoutResolving() throws Exception {
            trustAnchorPublishesNoHistoricalKeys();
            val chain = List.of(relyingPartyConfiguration(), statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0),
                entityConfiguration(TRUST_ANCHOR, trustAnchorKey, Map.of("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(), Map.of()))));
            val service = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3))
                .resolveSentTrustChain(RELYING_PARTY, chain).orElseThrow();
            assertEquals(RELYING_PARTY, service.getClientId());
            assertTrue(service.getJwks().contains("rp-oidc"));
            assertTrue(StringUtils.isNumeric(service.getProperties()
                .get(OidcFederationDefaultTrustChainResolver.OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT).getValue(String.class)));
            verify(retriever, never()).fetchEntityConfiguration(new EntityID(RELYING_PARTY));
            verify(retriever).fetchEntityConfiguration(new EntityID(TRUST_ANCHOR));

            val throughIntermediate = List.of(relyingPartyConfiguration(),
                statement(intermediateKey, INTERMEDIATE, RELYING_PARTY, relyingPartyKey, 0),
                statement(trustAnchorKey, TRUST_ANCHOR, INTERMEDIATE, intermediateKey, 0));
            assertTrue(resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(1))
                .resolveSentTrustChain(RELYING_PARTY, throughIntermediate).isPresent());
            assertTrue(resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(0))
                .resolveSentTrustChain(RELYING_PARTY, throughIntermediate).isEmpty());
        }

        /**
         * The keys a relying party publishes are its own data: expressions in them are never evaluated, so that a
         * federation member cannot read the environment of CAS through the key identifiers echoed in its tokens.
         */
        @Test
        void verifyKeysOfRelyingPartyAreNotEvaluated() throws Exception {
            trustAnchorPublishesNoHistoricalKeys();
            val keyId = "${#systemProperties['java.version']}-${#env['PATH']}";
            val encryptionKey = new RSAKeyGenerator(2048).keyID(keyId).keyUse(KeyUse.ENCRYPTION).generate().toPublicJWK();
            val chain = List.of(relyingPartyConfiguration(Map.of("jwks", new JWKSet(encryptionKey).toJSONObject())),
                statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0));
            val service = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3))
                .resolveSentTrustChain(RELYING_PARTY, chain).orElseThrow();
            val keys = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, new DefaultResourceLoader(),
                Optional.of(OidcJsonWebKeyUsage.ENCRYPTION)).orElseThrow();
            assertEquals(keyId, keys.getJsonWebKeys().getFirst().getKeyId());
        }

        /**
         * Every statement of a trust chain must name the key it is signed with (OpenID Federation 1.0, section 3).
         * Without a {@code kid}, verification tries every key of the trust anchor, so a statement signed with a key the
         * trust anchor has revoked, but that is still trusted, would escape the check of revoked keys.
         */
        @Test
        void verifyStatementsMustNameTheirSigningKey() throws Exception {
            trustAnchorPublishesNoHistoricalKeys();
            val resolver = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3));
            val now = Instant.now(Clock.systemUTC());
            val claims = new EntityStatementClaimsSet(new EntityID(TRUST_ANCHOR), new EntityID(RELYING_PARTY),
                Date.from(now.minusSeconds(60)), Date.from(now.plusSeconds(3600)), new JWKSet(relyingPartyKey.toPublicJWK())).toJWTClaimsSet();
            val unnamed = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("entity-statement+jwt")).build(), claims);
            unnamed.sign(new RSASSASigner(trustAnchorKey));
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(), unnamed.serialize())).isEmpty());

            val named = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("entity-statement+jwt"))
                .keyID(trustAnchorKey.getKeyID()).build(), claims);
            named.sign(new RSASSASigner(trustAnchorKey));
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(), named.serialize())).isPresent());
        }

        /**
         * A sent trust chain that cannot be used is refused without resolving the trust chains, which is left to the
         * registration.
         */
        @Test
        void verifyUnusableTrustChainIsRefused() throws Exception {
            when(retriever.fetchEntityConfiguration(any())).thenThrow(new ResolveException("Not reachable"));
            val trustAnchorKeys = new JWKSet(trustAnchorKey.toPublicJWK());
            val resolver = resolver(trustAnchorKeys, new TrustChainConstraints(3));
            val valid = List.of(relyingPartyConfiguration(), statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0));
            assertTrue(resolver.resolveSentTrustChain("https://other.example.org", valid).isEmpty());
            assertTrue(resolver(null, new TrustChainConstraints(3)).resolveSentTrustChain(RELYING_PARTY, valid).isEmpty());
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(),
                statement(generateKey("forged"), TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0))).isEmpty());
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(),
                statement(trustAnchorKey, "https://other-ta.example.org", RELYING_PARTY, relyingPartyKey, 0))).isEmpty());
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(),
                statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, -7200))).isEmpty());
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of("not-a-statement", "either")).isEmpty());
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of()).isEmpty());
            verify(retriever, never()).fetchEntityConfiguration(any());
        }

        /**
         * The {@code constraints} of every subordinate statement apply to the entities below its issuer (section 6.2).
         */
        @Test
        void verifyConstraintsOfSubordinateStatements() throws Exception {
            trustAnchorPublishesNoHistoricalKeys();
            val resolver = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3));
            for (val accepted : List.<Map<String, Object>>of(
                Map.of("max_path_length", 0),
                Map.of("max_path_length", 2147483648L),
                Map.of("naming_constraints", Map.of("permitted", List.of(".example.org"))),
                Map.of("naming_constraints", Map.of("permitted", List.of("rp.example.org"), "excluded", List.of(".example.com"))),
                Map.of("allowed_entity_types", List.of("openid_provider", "openid_relying_party")),
                Map.of("unknown_constraint", true))) {
                assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(),
                    statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0, accepted))).isPresent(), accepted::toString);
            }
            for (val refused : List.<Map<String, Object>>of(
                Map.of("max_path_length", -1),
                Map.of("max_path_length", 0.5),
                Map.of("max_path_length", "1"),
                Map.of("naming_constraints", Map.of("permitted", List.of(".example.com"))),
                Map.of("naming_constraints", Map.of("permitted", List.of("example.org"))),
                Map.of("naming_constraints", Map.of("permitted", List.of(".example.org"), "excluded", List.of("rp.example.org"))),
                Map.of("naming_constraints", Map.of("permitted", "example.org")),
                Map.of("allowed_entity_types", List.of("openid_provider")),
                Map.of("allowed_entity_types", List.of()))) {
                assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(),
                    statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0, refused))).isEmpty(), refused::toString);
            }

            val trustAnchorAllowsOne = List.of(relyingPartyConfiguration(),
                statement(intermediateKey, INTERMEDIATE, RELYING_PARTY, relyingPartyKey, 0, Map.of("max_path_length", 0)),
                statement(trustAnchorKey, TRUST_ANCHOR, INTERMEDIATE, intermediateKey, 0, Map.of("max_path_length", 1)));
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, trustAnchorAllowsOne).isPresent());
            val trustAnchorAllowsNone = List.of(relyingPartyConfiguration(),
                statement(intermediateKey, INTERMEDIATE, RELYING_PARTY, relyingPartyKey, 0),
                statement(trustAnchorKey, TRUST_ANCHOR, INTERMEDIATE, intermediateKey, 0, Map.of("max_path_length", 0)));
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, trustAnchorAllowsNone).isEmpty());
            val trustAnchorExcludesIntermediate = List.of(relyingPartyConfiguration(),
                statement(intermediateKey, INTERMEDIATE, RELYING_PARTY, relyingPartyKey, 0),
                statement(trustAnchorKey, TRUST_ANCHOR, INTERMEDIATE, intermediateKey, 0,
                    Map.of("naming_constraints", Map.of("excluded", List.of("intermediate.example.org")))));
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, trustAnchorExcludesIntermediate).isEmpty());
        }

        /**
         * The keys a trust anchor is trusted with out of band may still list a key it has since revoked (section 8.7):
         * a chain whose trust anchor statement is signed with it is refused. The historical keys are those the trust
         * anchor advertises in the entity configuration fetched from it, not in one the chain carries, which the holder of
         * the revoked key could produce to advertise no historical keys, or historical keys of its own. A key retired before the statement was issued refuses it as well. Keys that are
         * only expired since do not refuse the chain, nor does a trust anchor that advertises no historical keys. A trust
         * anchor configuration or historical keys that cannot be obtained or verified, historical keys without
         * {@code exp} or served over plain {@code http}, make the resolution unavailable, since whether the key was
         * revoked cannot be told. Historical keys are kept for a while by each resolver.
         */
        @Test
        void verifyTrustAnchorKeyRevokedInHistoricalKeys() throws Exception {
            val chain = List.of(relyingPartyConfiguration(), statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0));
            val now = Instant.now(Clock.systemUTC());
            val revokedKey = new JSONObject(trustAnchorKey.toPublicJWK().toJSONObject());
            revokedKey.put("exp", now.getEpochSecond());
            revokedKey.put("revoked", Map.of("revoked_at", now.minusSeconds(60).getEpochSecond(), "reason", "compromised"));
            val expiredKey = new JSONObject(trustAnchorKey.toPublicJWK().toJSONObject());
            expiredKey.put("exp", now.getEpochSecond());

            try (val webServer = new MockWebServer(true, new ByteArrayResource(historicalKeys(trustAnchorKey, revokedKey).getBytes(StandardCharsets.UTF_8)))) {
                webServer.start();
                val endpoint = "https://localhost:%s/federation_historical_keys".formatted(webServer.getPort());
                when(retriever.fetchEntityConfiguration(new EntityID(TRUST_ANCHOR))).thenReturn(EntityStatement.parse(
                    entityConfiguration(TRUST_ANCHOR, trustAnchorKey, Map.of("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(),
                        Map.of("federation_historical_keys_endpoint", endpoint))))));
                val resolver = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3));
                assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, chain).isEmpty());

                val withConfigurationAdvertisingNoHistoricalKeys = List.of(relyingPartyConfiguration(),
                    statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0),
                    entityConfiguration(TRUST_ANCHOR, trustAnchorKey, Map.of("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(), Map.of()))));
                assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, withConfigurationAdvertisingNoHistoricalKeys).isEmpty());

                try (val forgedServer = new MockWebServer(true,
                    new ByteArrayResource(historicalKeys(trustAnchorKey, expiredKey).getBytes(StandardCharsets.UTF_8)))) {
                    forgedServer.start();
                    val withConfigurationAdvertisingOtherHistoricalKeys = List.of(relyingPartyConfiguration(),
                        statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0),
                        entityConfiguration(TRUST_ANCHOR, trustAnchorKey, Map.of("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(),
                            Map.of("federation_historical_keys_endpoint", "https://localhost:%s/forged".formatted(forgedServer.getPort()))))));
                    assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, withConfigurationAdvertisingOtherHistoricalKeys).isEmpty());
                }

                webServer.responseBody(historicalKeys(trustAnchorKey, expiredKey));
                assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, chain).isEmpty());
                assertTrue(resolveSentTrustChain(chain).isPresent());
                webServer.responseBody(historicalKeys(generateKey("ta"), revokedKey));
                assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolveSentTrustChain(chain));

                val retiredBeforeIssued = new JSONObject(trustAnchorKey.toPublicJWK().toJSONObject());
                retiredBeforeIssued.put("exp", now.minus(Duration.ofDays(2)).getEpochSecond());
                webServer.responseBody(historicalKeys(trustAnchorKey, retiredBeforeIssued));
                assertTrue(resolveSentTrustChain(chain).isEmpty());
                webServer.responseBody(historicalKeys(trustAnchorKey, new JSONObject(trustAnchorKey.toPublicJWK().toJSONObject())));
                assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolveSentTrustChain(chain));

                webServer.responseBody(historicalKeys(trustAnchorKey, expiredKey));
                when(retriever.fetchEntityConfiguration(new EntityID(TRUST_ANCHOR))).thenReturn(EntityStatement.parse(
                    entityConfiguration(TRUST_ANCHOR, trustAnchorKey, Map.of("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(),
                        Map.of("federation_historical_keys_endpoint", endpoint.replace("https:", "http:")))))));
                assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolveSentTrustChain(chain));
            }
            assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolveSentTrustChain(chain));

            when(retriever.fetchEntityConfiguration(new EntityID(TRUST_ANCHOR))).thenThrow(new ResolveException("Not reachable"));
            assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolveSentTrustChain(chain));
            trustAnchorPublishesNoHistoricalKeys();
            assertTrue(resolveSentTrustChain(chain).isPresent());
        }

        /**
         * The historical keys of a trust anchor are kept for a while once read, while a failure to read them is not.
         */
        @Test
        void verifyTrustAnchorHistoricalKeysAreKept() throws Exception {
            val chain = List.of(relyingPartyConfiguration(), statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0));
            when(retriever.fetchEntityConfiguration(new EntityID(TRUST_ANCHOR))).thenThrow(new ResolveException("Not reachable"));
            val resolver = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3));
            assertThrows(OidcFederationResolutionUnavailableException.class, () -> resolver.resolveSentTrustChain(RELYING_PARTY, chain));

            trustAnchorPublishesNoHistoricalKeys();
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, chain).isPresent());
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, chain).isPresent());
            verify(retriever, times(2)).fetchEntityConfiguration(new EntityID(TRUST_ANCHOR));
        }

        private Optional<OidcRegisteredService> resolveSentTrustChain(final List<String> chain) throws Exception {
            return resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3)).resolveSentTrustChain(RELYING_PARTY, chain);
        }

        private void trustAnchorPublishesNoHistoricalKeys() throws Exception {
            when(retriever.fetchEntityConfiguration(any())).thenThrow(new ResolveException("Not reachable"));
            doReturn(EntityStatement.parse(entityConfiguration(TRUST_ANCHOR, trustAnchorKey,
                Map.of("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(), Map.of())))))
                .when(retriever).fetchEntityConfiguration(new EntityID(TRUST_ANCHOR));
        }

        private String historicalKeys(final RSAKey signingKey, final Map<String, Object> key) throws Exception {
            val claims = new JWTClaimsSet.Builder()
                .issuer(TRUST_ANCHOR)
                .issueTime(new Date())
                .claim("keys", List.of(key))
                .build();
            val jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID())
                .type(new JOSEObjectType("jwk-set+jwt")).build(), claims);
            jwt.sign(new RSASSASigner(signingKey));
            return jwt.serialize();
        }

        /**
         * Metadata policies (section 6.1): the immediate superior's metadata applies first, then the policies merged from
         * the trust anchor down, with {@code scope} handled as a list; operators that are not understood are ignored
         * unless critical, and policies that cannot be merged invalidate the chain.
         */
        @Test
        void verifyMetadataPolicies() throws Exception {
            trustAnchorPublishesNoHistoricalKeys();
            val resolver = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3));
            val relyingParty = relyingPartyConfiguration(Map.of("scope", "openid email profile", "contacts", List.of("rp@example.org")));
            val metadataPolicy = Map.<String, Object>of(EntityType.OPENID_RELYING_PARTY.getValue(), Map.of(
                "scope", Map.of("subset_of", List.of("openid", "email")),
                "contacts", Map.of("add", List.of("ops@example.org")),
                "token_endpoint_auth_method", Map.of("one_of", List.of("private_key_jwt"), "essential", true),
                "client_name", Map.of("regexp", "^[A-Za-z ]+$")));
            OidcFederationMetadataPolicies.validate(metadataPolicy);

            val service = resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingParty,
                statementWithClaims(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, Map.of("metadata_policy", metadataPolicy)))).orElseThrow();
            assertEquals(Set.of("openid", "email"), service.getScopes());
            assertTrue(service.getContacts().stream().anyMatch(contact -> "ops@example.org".equals(contact.getEmail())));
            assertTrue(service.getContacts().stream().anyMatch(contact -> "rp@example.org".equals(contact.getEmail())));

            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingParty, statementWithClaims(trustAnchorKey, TRUST_ANCHOR,
                RELYING_PARTY, relyingPartyKey, Map.of("metadata_policy", metadataPolicy, "metadata_policy_crit", List.of("regexp"))))).isEmpty());

            val asserted = resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingParty, statementWithClaims(trustAnchorKey, TRUST_ANCHOR,
                RELYING_PARTY, relyingPartyKey, Map.of("metadata", Map.of(EntityType.OPENID_RELYING_PARTY.getValue(),
                    Map.of("client_name", "Asserted by the trust anchor")))))).orElseThrow();
            assertEquals("Asserted by the trust anchor", asserted.getName());

            assertEquals("Named by both", resolver.resolveSentTrustChain(RELYING_PARTY,
                namedByIntermediateAndTrustAnchor(relyingParty, "Named by both", "Named by both")).orElseThrow().getName());
            val conflicting = namedByIntermediateAndTrustAnchor(relyingParty, "Named by the intermediate", "Named by the trust anchor");
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, conflicting).isEmpty());
            assertThrows(PolicyViolationException.class, () -> OidcFederationMetadataPolicies.resolveMetadata(
                TrustChain.parseSerialized(conflicting), EntityType.OPENID_RELYING_PARTY));

            assertThrows(Exception.class, () -> OidcFederationMetadataPolicies.validate(Map.of(EntityType.OPENID_RELYING_PARTY.getValue(),
                Map.of("scope", Map.of("value", "openid", "one_of", List.of("email"))))));
        }

        /**
         * A relying party without redirect URIs is not registered, and a post-logout redirect URI that contains a comma is
         * left out, since the logout URLs of a service are a comma-separated list and it would split into other URIs.
         */
        @Test
        void verifyRedirectAndPostLogoutUris() throws Exception {
            trustAnchorPublishesNoHistoricalKeys();
            val resolver = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3));
            val statement = statement(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey, 0);
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY,
                List.of(relyingPartyConfiguration(Map.of("redirect_uris", List.of())), statement)).isEmpty());

            val service = resolver.resolveSentTrustChain(RELYING_PARTY, List.of(relyingPartyConfiguration(Map.of("post_logout_redirect_uris",
                List.of(RELYING_PARTY + "/bye", RELYING_PARTY + "/bye?next=1,https://elsewhere.example.org"))), statement)).orElseThrow();
            assertEquals(RELYING_PARTY + "/bye", service.getLogoutUrl());
        }

        /**
         * The metadata superiors assert, and their policies, apply only to the entity types the subject publishes in its
         * entity configuration (section 3): they cannot make an entity without {@code openid_relying_party} metadata a
         * relying party.
         */
        @Test
        void verifySuperiorsCannotAddEntityTypes() throws Exception {
            trustAnchorPublishesNoHistoricalKeys();
            val resolver = resolver(new JWKSet(trustAnchorKey.toPublicJWK()), new TrustChainConstraints(3));
            val withoutRelyingPartyMetadata = entityConfiguration(RELYING_PARTY, relyingPartyKey, Map.of(
                "authority_hints", List.of(TRUST_ANCHOR),
                "metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(), Map.of("organization_name", "Not a relying party"))));
            val relyingPartyMetadata = Map.<String, Object>of(
                "client_name", "Asserted by the trust anchor",
                "redirect_uris", List.of(RELYING_PARTY + "/callback"),
                "jwks", new JWKSet(generateKey("rp-oidc").toPublicJWK()).toJSONObject());
            val asserted = statementWithClaims(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey,
                Map.of("metadata", Map.of(EntityType.OPENID_RELYING_PARTY.getValue(), relyingPartyMetadata)));
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(withoutRelyingPartyMetadata, asserted)).isEmpty());
            val defaulted = statementWithClaims(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, relyingPartyKey,
                Map.of("metadata_policy", Map.of(EntityType.OPENID_RELYING_PARTY.getValue(),
                    Map.of("client_name", Map.of("default", "Defaulted by the trust anchor")))));
            assertTrue(resolver.resolveSentTrustChain(RELYING_PARTY, List.of(withoutRelyingPartyMetadata, defaulted)).isEmpty());

            assertEquals("Asserted by the trust anchor", resolver.resolveSentTrustChain(RELYING_PARTY,
                List.of(relyingPartyConfiguration(), asserted)).orElseThrow().getName());
        }

        /**
         * A chain whose intermediate and trust anchor each set {@code client_name} with {@code value}: either policy alone
         * applies to the metadata of the relying party, so only merging them can find that they conflict.
         */
        private List<String> namedByIntermediateAndTrustAnchor(final String relyingParty, final String byIntermediate,
                                                              final String byTrustAnchor)
                throws Exception {
            return List.of(relyingParty,
                statementWithClaims(intermediateKey, INTERMEDIATE, RELYING_PARTY, relyingPartyKey, Map.of("metadata_policy",
                    Map.of(EntityType.OPENID_RELYING_PARTY.getValue(), Map.of("client_name", Map.of("value", byIntermediate))))),
                statementWithClaims(trustAnchorKey, TRUST_ANCHOR, INTERMEDIATE, intermediateKey, Map.of("metadata_policy",
                    Map.of(EntityType.OPENID_RELYING_PARTY.getValue(), Map.of("client_name", Map.of("value", byTrustAnchor))))));
        }

        private OidcFederationDefaultTrustChainResolver resolver(@Nullable final JWKSet trustAnchorKeys, final TrustChainConstraints constraints) {
            val trustAnchors = new HashMap<EntityID, JWKSet>();
            trustAnchors.put(new EntityID(TRUST_ANCHOR), trustAnchorKeys);
            return new OidcFederationDefaultTrustChainResolver(List.of(new TrustChainResolver(trustAnchors, constraints, retriever)),
                new OidcFederationDefaultTrustMarkValidator(new OidcFederationProperties(), TEST_HTTP_CLIENT),
                new OidcFederationResolutionLimiter(new OidcFederationProperties()), new OidcFederationTrustAnchorKeys(TEST_HTTP_CLIENT));
        }

        private String relyingPartyConfiguration() throws Exception {
            return relyingPartyConfiguration(Map.of());
        }

        private String relyingPartyConfiguration(final Map<String, Object> metadata) throws Exception {
            val relyingParty = new JSONObject();
            relyingParty.put("client_name", "Sent trust chain RP");
            relyingParty.put("redirect_uris", List.of(RELYING_PARTY + "/callback"));
            relyingParty.put("scope", OidcConstants.StandardScopes.OPENID.getScope());
            relyingParty.put("token_endpoint_auth_method", ClientAuthenticationMethod.PRIVATE_KEY_JWT.getValue());
            relyingParty.put("jwks", new JWKSet(generateKey("rp-oidc").toPublicJWK()).toJSONObject());
            relyingParty.putAll(metadata);
            return entityConfiguration(RELYING_PARTY, relyingPartyKey, Map.of(
                "authority_hints", List.of(TRUST_ANCHOR),
                "metadata", Map.of(EntityType.OPENID_RELYING_PARTY.getValue(), relyingParty)));
        }

        private static String statement(final RSAKey issuerKey, final String issuer, final String subject,
                                        final RSAKey subjectKey, final long expirationOffset)
                throws Exception {
            return statement(issuerKey, issuer, subject, subjectKey, expirationOffset, Map.of());
        }

        private static String statement(final RSAKey issuerKey, final String issuer, final String subject,
                                        final RSAKey subjectKey, final long expirationOffset, final Map<String, Object> constraints)
                throws Exception {
            val now = Instant.now(Clock.systemUTC());
            val expiration = expirationOffset < 0 ? now.plusSeconds(expirationOffset) : now.plusSeconds(86400);
            val claims = new EntityStatementClaimsSet(new EntityID(issuer), new EntityID(subject),
                Date.from(now.minusSeconds(86400)), Date.from(expiration), new JWKSet(subjectKey.toPublicJWK()));
            if (!constraints.isEmpty()) {
                claims.setClaim("constraints", new JSONObject(constraints));
            }
            return EntityStatement.sign(claims, issuerKey).getSignedStatement().serialize();
        }

        private static String statementWithClaims(final RSAKey issuerKey, final String issuer, final String subject,
                                                  final RSAKey subjectKey, final Map<String, Object> extraClaims)
                throws Exception {
            val now = Instant.now(Clock.systemUTC());
            val claims = new EntityStatementClaimsSet(new EntityID(issuer), new EntityID(subject),
                Date.from(now.minusSeconds(60)), Date.from(now.plusSeconds(86400)), new JWKSet(subjectKey.toPublicJWK()));
            extraClaims.forEach(claims::setClaim);
            return EntityStatement.sign(claims, issuerKey).getSignedStatement().serialize();
        }

        private static String entityConfiguration(final String entityId, final RSAKey key, final Map<String, ?> extraClaims) throws Exception {
            val now = Instant.now(Clock.systemUTC());
            val claims = new EntityStatementClaimsSet(new EntityID(entityId), new EntityID(entityId),
                Date.from(now), Date.from(now.plusSeconds(86400)), new JWKSet(key.toPublicJWK()));
            extraClaims.forEach(claims::setClaim);
            return EntityStatement.sign(claims, key).getSignedStatement().serialize();
        }

        private static RSAKey generateKey(final String keyId) {
            return FunctionUtils.doUnchecked(() -> new RSAKeyGenerator(2048).keyID(keyId).generate());
        }
    }

    @Nested
    class TrustMarkTests {
        private static final String TRUST_ANCHOR = "https://ta.example.org";

        private static final String RELYING_PARTY = "https://rp.example.org";

        private static final String ISSUER = "https://issuer.example.org";

        private static final String OWNER = "https://owner.example.org";

        private static final String TYPE = "https://ta.example.org/trust-marks/member";

        private final RSAKey trustAnchorKey = generateKey("ta");

        private final RSAKey issuerKey = generateKey("issuer");

        private final RSAKey ownerKey = generateKey("owner");

        private final OidcFederationDefaultTrustMarkValidator validator = new OidcFederationDefaultTrustMarkValidator(
            new OidcFederationProperties().setRequiredTrustMarks(List.of(TYPE)), TEST_HTTP_CLIENT);

        @Test
        void verifyTrustMarkIssuedByTrustAnchor() throws Exception {
            val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of());
            val trustMark = trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of());
            assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));

            assertTrue(new OidcFederationDefaultTrustMarkValidator(new OidcFederationProperties(), TEST_HTTP_CLIENT)
                .hasRequiredTrustMarks(resolver(trustAnchor, Map.of()), trustChain(relyingParty(null), trustAnchor)));
            assertFalse(validator.hasRequiredTrustMarks(resolver(trustAnchor, Map.of()), trustChain(relyingParty(null), trustAnchor)));
        }

        @Test
        void verifyInvalidTrustMarksAreRefused() throws Exception {
            val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of());
            val now = Instant.now(Clock.systemUTC());
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark(trustAnchorKey, TRUST_ANCHOR, "https://other.example.org", TYPE, Map.of()), Map.of()));
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE + "/other", Map.of()), Map.of()));
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE,
                Map.of(JWTClaimNames.EXPIRATION_TIME, Date.from(now.minusSeconds(3600)))), Map.of()));
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE,
                Map.of(JWTClaimNames.ISSUED_AT, Date.from(now.plusSeconds(3600)))), Map.of()));
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark(issuerKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of()), Map.of()));
            assertFalse(hasRequiredTrustMarks(trustAnchor, sign(trustAnchorKey, JOSEObjectType.JWT,
                trustMarkClaims(TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of())), Map.of()));
            assertFalse(hasRequiredTrustMarks(trustAnchorConfiguration(Map.of(TYPE, List.of(ISSUER)), Map.of()),
                trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of()), Map.of()));
            assertFalse(hasRequiredTrustMarks(trustAnchorConfiguration(Map.of(), Map.of()),
                trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of()), Map.of()));
        }

        @Test
        void verifyTrustMarkFromIssuerWithItsOwnTrustChain() throws Exception {
            val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of()), Map.of());
            val issuer = entityConfiguration(ISSUER, issuerKey, Map.of());
            val trustMark = trustMark(issuerKey, ISSUER, RELYING_PARTY, TYPE, Map.of());
            assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of(ISSUER, trustChain(issuer, trustAnchor))));
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));

            val otherTrustAnchor = entityConfiguration("https://other-ta.example.org", generateKey("other"), Map.of());
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of(ISSUER, trustChain(issuer, otherTrustAnchor))));

            val excluded = trustChain(issuer, trustAnchor);
            when(excluded.getSuperiorStatements()).thenReturn(List.of(subordinateStatement(ISSUER, issuerKey,
                Map.of("naming_constraints", Map.of("excluded", List.of("issuer.example.org"))))));
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of(ISSUER, excluded)));
            val federationEntityOnly = trustChain(issuer, trustAnchor);
            when(federationEntityOnly.getSuperiorStatements()).thenReturn(List.of(subordinateStatement(ISSUER, issuerKey,
                Map.of("allowed_entity_types", List.of(), "naming_constraints", Map.of("permitted", List.of(".example.org"))))));
            assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of(ISSUER, federationEntityOnly)));
        }

        @Test
        void verifyDelegationForOwnedTrustMarkTypes() throws Exception {
            val owners = Map.<String, Object>of(TYPE, Map.of(JWTClaimNames.SUBJECT, OWNER,
                "jwks", new JWKSet(ownerKey.toPublicJWK()).toJSONObject()));
            val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), owners);
            assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of()), Map.of()));

            val delegationClaims = new JWTClaimsSet.Builder()
                .issuer(OWNER)
                .subject(TRUST_ANCHOR)
                .claim("trust_mark_type", TYPE)
                .issueTime(new Date())
                .build();
            val delegation = sign(ownerKey, OidcFederationDefaultTrustMarkValidator.TRUST_MARK_DELEGATION_JWT_TYPE, delegationClaims);
            assertTrue(hasRequiredTrustMarks(trustAnchor,
                trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of("delegation", delegation)), Map.of()));
            val forged = sign(issuerKey, OidcFederationDefaultTrustMarkValidator.TRUST_MARK_DELEGATION_JWT_TYPE, delegationClaims);
            assertFalse(hasRequiredTrustMarks(trustAnchor,
                trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of("delegation", forged)), Map.of()));
        }

        /**
         * A trust mark signed with a key its issuer has retired since stays valid, verified with that key from the
         * issuer's historical keys (OpenID Federation 1.0, section 8.7), unless the key was revoked, was retired before
         * the trust mark was issued, carries no {@code exp}, the historical keys are not served over {@code https}, or the
         * issuer publishes no historical keys. Historical keys are kept for a while per issuer and endpoint.
         */
        @Test
        void verifyTrustMarkSignedWithRetiredKey() throws Exception {
            val retiredKey = generateKey("ta-retired");
            val trustMark = trustMark(retiredKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of());
            val retiredAt = Date.from(Instant.now(Clock.systemUTC()).plusSeconds(60));
            val retired = new RSAKey.Builder(retiredKey.toPublicJWK()).expirationTime(retiredAt).build();
            try (val webServer = new MockWebServer(true, new ByteArrayResource(historicalKeys(retired).getBytes(StandardCharsets.UTF_8)))) {
                webServer.start();
                val endpoint = "https://localhost:%s/historical".formatted(webServer.getPort());
                val trustAnchor = trustAnchorWithHistoricalKeys(endpoint + "/retired");
                assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));
                webServer.responseBody(historicalKeys(new RSAKey.Builder(retiredKey.toPublicJWK())
                    .expirationTime(Date.from(Instant.now(Clock.systemUTC()).minusSeconds(3600))).build()));
                assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()), "Historical keys are kept for a while");

                assertFalse(hasRequiredTrustMarks(trustAnchorWithHistoricalKeys(endpoint + "/retired-before"), trustMark, Map.of()));
                webServer.responseBody(historicalKeys(new RSAKey.Builder(retired)
                    .keyRevocation(new KeyRevocation(new Date(), KeyRevocation.Reason.COMPROMISED)).build()));
                assertFalse(hasRequiredTrustMarks(trustAnchorWithHistoricalKeys(endpoint + "/revoked"), trustMark, Map.of()));
                webServer.responseBody(historicalKeys(retiredKey.toPublicJWK()));
                assertFalse(hasRequiredTrustMarks(trustAnchorWithHistoricalKeys(endpoint + "/without-exp"), trustMark, Map.of()));
                webServer.responseBody(historicalKeys(retired));
                assertFalse(hasRequiredTrustMarks(trustAnchorWithHistoricalKeys(endpoint.replace("https:", "http:") + "/plain"),
                    trustMark, Map.of()));
            }
            val withoutHistoricalKeys = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of());
            assertFalse(hasRequiredTrustMarks(withoutHistoricalKeys, trustMark, Map.of()));
        }

        @Test
        void verifyTrustMarkStatusEndpoint() throws Exception {
            val trustMark = trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of());
            val active = statusResponse(trustMark, "active");
            try (val webServer = new MockWebServer(true, new ByteArrayResource(active.getBytes(StandardCharsets.UTF_8)))) {
                webServer.start();
                val endpoint = "https://localhost:%s/status".formatted(webServer.getPort());
                val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of(),
                    Map.of("federation_trust_mark_status_endpoint", endpoint));
                assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));
                assertFalse(hasRequiredTrustMarks(trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of(),
                    Map.of("federation_trust_mark_status_endpoint", endpoint.replace("https:", "http:"))), trustMark, Map.of()));
                webServer.responseBody(statusResponse(trustMark, "revoked"));
                assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));
                webServer.responseBody(statusResponse(trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE,
                    Map.of("ref", "https://ta.example.org/other")), "active"));
                assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));
            }
        }

        /**
         * A trust mark whose validity cannot be told now, because its issuer's status endpoint or historical keys endpoint
         * answers with a server error, makes the validation unavailable only when no other trust mark of its type is
         * valid and no required type is missing; the trust marks of a resolution leave it out and say so. A trust anchor that
         * cannot be reached while the chain of an issuer is resolved makes its trust marks unavailable too.
         */
        @Test
        void verifyTrustMarksThatCannotBeToldNow() throws Exception {
            try (val webServer = new MockWebServer(true, new ByteArrayResource("{}".getBytes(StandardCharsets.UTF_8)))) {
                webServer.start();
                webServer.responseStatus(HttpStatus.SERVICE_UNAVAILABLE);
                val endpoint = "https://localhost:%s".formatted(webServer.getPort());
                val otherType = TYPE + "/other";
                val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR, ISSUER), otherType, List.of(TRUST_ANCHOR)), Map.of());
                val issuer = entityConfiguration(ISSUER, issuerKey, Map.of("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(),
                    Map.of("federation_trust_mark_status_endpoint", endpoint + "/status"))));
                val resolver = resolver(trustAnchor, Map.of(ISSUER, trustChain(issuer, trustAnchor)));
                val unavailable = Map.of("trust_mark_type", TYPE, "trust_mark", trustMark(issuerKey, ISSUER, RELYING_PARTY, TYPE, Map.of()));
                val expired = Map.of("trust_mark_type", TYPE, "trust_mark", trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE,
                    Map.of(JWTClaimNames.EXPIRATION_TIME, Date.from(Instant.now(Clock.systemUTC()).minusSeconds(3600)))));
                val valid = Map.of("trust_mark_type", TYPE, "trust_mark", trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of()));
                val validOfOtherType = Map.of("trust_mark_type", otherType,
                    "trust_mark", trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, otherType, Map.of()));

                assertThrows(OidcFederationResolutionUnavailableException.class,
                    () -> validator.hasRequiredTrustMarks(resolver, trustChain(relyingParty(List.of(unavailable)), trustAnchor)));
                assertTrue(validator.hasRequiredTrustMarks(resolver, trustChain(relyingParty(List.of(unavailable, valid)), trustAnchor)));
                assertTrue(validator.hasRequiredTrustMarks(resolver, trustChain(relyingParty(List.of(expired, valid)), trustAnchor)));
                assertFalse(validator.hasRequiredTrustMarks(resolver, trustChain(relyingParty(List.of(expired)), trustAnchor)));

                val bothTypes = new OidcFederationDefaultTrustMarkValidator(
                    new OidcFederationProperties().setRequiredTrustMarks(List.of(TYPE, otherType)), TEST_HTTP_CLIENT);
                assertFalse(bothTypes.hasRequiredTrustMarks(resolver, trustChain(relyingParty(List.of(unavailable)), trustAnchor)));
                assertThrows(OidcFederationResolutionUnavailableException.class, () -> bothTypes.hasRequiredTrustMarks(resolver,
                    trustChain(relyingParty(List.of(unavailable, validOfOtherType)), trustAnchor)));

                val resolved = validator.resolveValidTrustMarks(resolver, trustChain(relyingParty(List.of(unavailable, valid)), trustAnchor));
                assertEquals(List.of(valid), resolved.trustMarks());
                assertFalse(resolved.complete());
                assertTrue(validator.resolveValidTrustMarks(resolver, trustChain(relyingParty(List.of(expired, valid)), trustAnchor)).complete());

                val unreachable = resolver(trustAnchor, Map.of());
                when(unreachable.resolveTrustChains(any(EntityID.class))).thenAnswer(_ -> {
                    OidcFederationResolutionLimiter.currentBudget().orElseThrow().markUnreachable(TRUST_ANCHOR);
                    throw new ResolveException("Trust anchor answers 503");
                });
                new OidcFederationResolutionLimiter(new OidcFederationProperties()).execute(() -> {
                    assertThrows(OidcFederationResolutionUnavailableException.class,
                        () -> validator.hasRequiredTrustMarks(unreachable, trustChain(relyingParty(List.of(unavailable)), trustAnchor)));
                    assertFalse(validator.hasRequiredTrustMarks(resolver, trustChain(relyingParty(List.of(expired)), trustAnchor)),
                        "A trust anchor found unreachable by an earlier step tells nothing about this one");
                    return null;
                });

                val signedWithRetiredKey = trustMark(generateKey("ta-unavailable"), TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of());
                assertThrows(OidcFederationResolutionUnavailableException.class, () -> hasRequiredTrustMarks(
                    trustAnchorWithHistoricalKeys(endpoint + "/historical-unavailable"), signedWithRetiredKey, Map.of()));
            }
        }

        /**
         * Trust marks are validated against the configuration fetched from the trust anchor, never the one a chain
         * carries, which could list issuers of its own if signed with a revoked but still trusted key.
         */
        @Test
        void verifyTrustAnchorConfigurationIsNotTakenFromTheChain() throws Exception {
            val trustMark = trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of());
            val genuine = trustAnchorConfiguration(Map.of(), Map.of());
            val carried = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of());
            val chain = trustChain(relyingParty(List.of(Map.of("trust_mark_type", TYPE, "trust_mark", trustMark))), genuine);
            when(chain.getTrustAnchorConfiguration()).thenReturn(carried);
            assertFalse(validator.hasRequiredTrustMarks(resolver(genuine, Map.of()), chain));
            assertTrue(validator.resolveValidTrustMarks(resolver(genuine, Map.of()), chain).trustMarks().isEmpty());
            assertTrue(validator.hasRequiredTrustMarks(resolver(carried, Map.of()), chain));
            assertEquals(1, validator.resolveValidTrustMarks(resolver(carried, Map.of()), chain).trustMarks().size());
        }

        private EntityStatement trustAnchorWithHistoricalKeys(final String endpoint) throws Exception {
            return trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of(), Map.of("federation_historical_keys_endpoint", endpoint));
        }

        private boolean hasRequiredTrustMarks(final EntityStatement trustAnchor, final String trustMark,
                                              final Map<String, TrustChain> issuerChains)
                throws Exception {
            val relyingParty = relyingParty(List.of(Map.of("trust_mark_type", TYPE, "trust_mark", trustMark)));
            return validator.hasRequiredTrustMarks(resolver(trustAnchor, issuerChains), trustChain(relyingParty, trustAnchor));
        }

        private TrustChainResolver resolver(final EntityStatement trustAnchor, final Map<String, TrustChain> issuerChains) throws Exception {
            val resolver = mock(TrustChainResolver.class);
            when(resolver.getTrustAnchors()).thenReturn(Map.of(new EntityID(TRUST_ANCHOR), new JWKSet(trustAnchorKey.toPublicJWK())));
            val retriever = mock(EntityStatementRetriever.class);
            when(retriever.fetchEntityConfiguration(new EntityID(TRUST_ANCHOR))).thenReturn(trustAnchor);
            when(resolver.getEntityStatementRetriever()).thenReturn(retriever);
            when(resolver.resolveTrustChains(any(EntityID.class))).thenAnswer(invocation -> {
                val chains = new TrustChainSet();
                Optional.ofNullable(issuerChains.get(invocation.getArgument(0, EntityID.class).getValue())).ifPresent(chains::add);
                return chains;
            });
            return resolver;
        }

        private TrustChain trustChain(final EntityStatement leaf, final EntityStatement trustAnchor) {
            val chain = mock(TrustChain.class);
            when(chain.getLeafConfiguration()).thenReturn(leaf);
            when(chain.getTrustAnchorEntityID()).thenReturn(trustAnchor.getEntityID());
            when(chain.length()).thenReturn(2);
            return chain;
        }

        private EntityStatement relyingParty(final List<Map<String, String>> trustMarks) throws Exception {
            return entityConfiguration(RELYING_PARTY, generateKey("rp"), trustMarks == null ? Map.of() : Map.of("trust_marks", trustMarks));
        }

        private EntityStatement trustAnchorConfiguration(final Map<String, List<String>> issuers, final Map<String, Object> owners) throws Exception {
            return trustAnchorConfiguration(issuers, owners, Map.of());
        }

        private EntityStatement trustAnchorConfiguration(final Map<String, List<String>> issuers, final Map<String, Object> owners,
                                                         final Map<String, Object> federationEntity)
                throws Exception {
            val claims = new LinkedHashMap<String, Object>();
            claims.put("trust_mark_issuers", issuers);
            if (!owners.isEmpty()) {
                claims.put("trust_mark_owners", owners);
            }
            claims.put("metadata", Map.of(EntityType.FEDERATION_ENTITY.getValue(), federationEntity));
            return entityConfiguration(TRUST_ANCHOR, trustAnchorKey, claims);
        }

        private static EntityStatement entityConfiguration(final String entityId, final RSAKey key,
                                                           final Map<String, ?> extraClaims)
                throws Exception {
            val now = Instant.now(Clock.systemUTC());
            val claims = new EntityStatementClaimsSet(new EntityID(entityId), new EntityID(entityId),
                Date.from(now), Date.from(now.plusSeconds(86400)), new JWKSet(key.toPublicJWK()));
            extraClaims.forEach(claims::setClaim);
            return EntityStatement.sign(claims, key);
        }

        private EntityStatement subordinateStatement(final String subject, final RSAKey subjectKey,
                                                     final Map<String, Object> constraints)
                throws Exception {
            val now = Instant.now(Clock.systemUTC());
            val claims = new EntityStatementClaimsSet(new EntityID(TRUST_ANCHOR), new EntityID(subject),
                Date.from(now), Date.from(now.plusSeconds(86400)), new JWKSet(subjectKey.toPublicJWK()));
            claims.setClaim("constraints", new JSONObject(constraints));
            return EntityStatement.sign(claims, trustAnchorKey);
        }

        private static JWTClaimsSet trustMarkClaims(final String issuer, final String subject, final String type,
                                                    final Map<String, Object> overrides) {
            val builder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(subject)
                .claim("trust_mark_type", type)
                .issueTime(new Date());
            overrides.forEach(builder::claim);
            return builder.build();
        }

        private static String trustMark(final RSAKey key, final String issuer, final String subject, final String type,
                                         final Map<String, Object> overrides)
                throws Exception {
            return sign(key, OidcFederationDefaultTrustMarkValidator.TRUST_MARK_JWT_TYPE, trustMarkClaims(issuer, subject, type, overrides));
        }

        private String historicalKeys(final JWK key) throws Exception {
            val claims = new JWTClaimsSet.Builder()
                .issuer(TRUST_ANCHOR)
                .issueTime(new Date())
                .claim("keys", List.of(key.toJSONObject()))
                .build();
            return sign(trustAnchorKey, new JOSEObjectType("jwk-set+jwt"), claims);
        }

        private String statusResponse(final String trustMark, final String status) throws Exception {
            val claims = new JWTClaimsSet.Builder()
                .issuer(TRUST_ANCHOR)
                .issueTime(new Date())
                .claim("trust_mark", trustMark)
                .claim("status", status)
                .build();
            return sign(trustAnchorKey, OidcFederationDefaultTrustMarkValidator.TRUST_MARK_STATUS_RESPONSE_JWT_TYPE, claims);
        }

        private static String sign(final RSAKey key, final JOSEObjectType type, final JWTClaimsSet claims) throws Exception {
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).type(type).build();
            val jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        }

        private static RSAKey generateKey(final String keyId) {
            return FunctionUtils.doUnchecked(() -> new RSAKeyGenerator(2048).keyID(keyId).generate());
        }
    }
}
