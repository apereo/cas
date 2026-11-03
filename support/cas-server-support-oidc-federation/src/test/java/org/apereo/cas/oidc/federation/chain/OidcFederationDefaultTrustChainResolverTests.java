package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustAnchorProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
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
}
