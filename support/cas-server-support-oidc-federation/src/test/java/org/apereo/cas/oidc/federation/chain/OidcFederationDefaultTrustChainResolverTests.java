package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustAnchorProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationDefaultTrustMarkValidator;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.policy.MetadataPolicy;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.ResolveException;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainSet;
import com.nimbusds.openid.connect.sdk.rp.ApplicationType;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import lombok.val;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
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
            assertEquals("https://example2\\.com\\?parameter=false,https://example\\.com\\?parameter=true", registeredService.getServiceId());
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
                val trustChainSet = mock(TrustChainSet.class);
                val trustChain = mock(TrustChain.class);
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
                when(trustChainSet.getShortest()).thenReturn(trustChain);
                when(resolver.resolveTrustChains(any(EntityID.class))).thenReturn(trustChainSet);
                return resolver;
            }
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
            new OidcFederationProperties().setRequiredTrustMarks(List.of(TYPE)));

        @Test
        void verifyTrustMarkIssuedByTrustAnchor() throws Exception {
            val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of());
            val trustMark = trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of());
            assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));

            assertTrue(new OidcFederationDefaultTrustMarkValidator(new OidcFederationProperties())
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

        @Test
        void verifyTrustMarkStatusEndpoint() throws Exception {
            val trustMark = trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE, Map.of());
            val active = statusResponse(trustMark, "active");
            try (val webServer = new MockWebServer(new ByteArrayResource(active.getBytes(StandardCharsets.UTF_8)),
                "application/trust-mark-status-response+jwt")) {
                webServer.start();
                val endpoint = "http://localhost:%s/status".formatted(webServer.getPort());
                val trustAnchor = trustAnchorConfiguration(Map.of(TYPE, List.of(TRUST_ANCHOR)), Map.of(),
                    Map.of("federation_trust_mark_status_endpoint", endpoint));
                assertTrue(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));
                webServer.responseBody(statusResponse(trustMark, "revoked"));
                assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));
                webServer.responseBody(statusResponse(trustMark(trustAnchorKey, TRUST_ANCHOR, RELYING_PARTY, TYPE,
                    Map.of("ref", "https://ta.example.org/other")), "active"));
                assertFalse(hasRequiredTrustMarks(trustAnchor, trustMark, Map.of()));
            }
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
