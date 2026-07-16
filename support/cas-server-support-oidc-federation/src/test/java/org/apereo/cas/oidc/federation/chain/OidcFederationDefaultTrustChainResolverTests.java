package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.policy.MetadataPolicy;
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
        @Test
        void verifyNoTrust() throws Exception {
            assertTrue(oidcFederationTrustChainResolver.resolveTrustChains(UUID.randomUUID().toString()).isEmpty());
            assertTrue(oidcFederationTrustChainResolver.resolveTrustChains("https://rp.example.com").isEmpty());
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
                final CasConfigurationProperties casProperties) throws Exception {

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
