package org.apereo.cas.support.pac4j.clients;

import module java.base;
import org.apereo.cas.config.CasDelegatedAuthenticationOidcAutoConfiguration;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.method.PrivateKeyJwtClientAuthnMethodConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedIdentityProviderFactoryOidcTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Delegation")
class DefaultDelegatedIdentityProviderFactoryOidcTests {

    @TestPropertySource(properties = "cas.custom.properties.delegation-test.enabled=false")
    @ImportAutoConfiguration(CasDelegatedAuthenticationOidcAutoConfiguration.class)
    abstract static class BaseTests extends BaseDelegatedClientFactoryTests {
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.github.scope=user",
        "cas.authn.pac4j.github.id=12345",
        "cas.authn.pac4j.github.secret=s3cr3t",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class GitHubClients extends BaseTests {
        @Test
        void verifyGitHubClient() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
            val client = (GitHubClient) clients.getFirst();
            assertEquals("user", client.getScope());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oauth2[0].id=123456",
        "cas.authn.pac4j.oauth2[0].secret=s3cr3t",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class OAuth20Clients extends BaseTests {
        @Test
        void verifyFactory() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oidc[0].apple.private-key=classpath:apple.pem",
        "cas.authn.pac4j.oidc[0].apple.private-key-id=VB4MYGJ3TQ",
        "cas.authn.pac4j.oidc[0].apple.team-id=67D9XQG2LJ",
        "cas.authn.pac4j.oidc[0].apple.response_type=code id_token",
        "cas.authn.pac4j.oidc[0].apple.response_mode=form_post",
        "cas.authn.pac4j.oidc[0].apple.scope=openid name email",
        "cas.authn.pac4j.oidc[0].apple.discovery-uri=https://localhost:8443/.well-known/openid-configuration",
        "cas.authn.pac4j.oidc[0].apple.id=pac4j",
        "cas.authn.pac4j.oidc[0].apple.use-nonce=true",
        "cas.authn.pac4j.oidc[0].apple.enabled=true",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class AppleClients extends BaseTests {
        @Test
        void verifyClient() {
            val clients = delegatedIdentityProviderFactory.build();

            assertEquals(1, clients.size());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oidc[0].generic.id=123",
        "cas.authn.pac4j.oidc[0].generic.secret=123",
        "cas.authn.pac4j.oidc[0].generic.response_mode=query",
        "cas.authn.pac4j.oidc[0].generic.response_type=none",
        "cas.authn.pac4j.oidc[0].generic.scope=scope1",
        "cas.authn.pac4j.oidc[0].generic.token-expiration-advance=PT5S",
        "cas.authn.pac4j.oidc[0].generic.preferred-jws-algorithm=RS256",
        "cas.authn.pac4j.oidc[0].generic.discovery-uri=https://dev-425954.oktapreview.com/.well-known/openid-configuration",
        "cas.authn.pac4j.oidc[0].generic.supported-client-authentication-methods=client_secret_post",
        "cas.authn.pac4j.oidc[0].generic.client-authentication-method=client_secret_post",
        "cas.authn.pac4j.oidc[0].generic.mapped-claims[0]=claim1->attribute1",

        "cas.authn.pac4j.oidc[1].google.id=123",
        "cas.authn.pac4j.oidc[1].google.secret=123",
        "cas.authn.pac4j.oidc[1].google.discovery-uri=https://localhost:8443/.well-known/openid-configuration",

        "cas.authn.pac4j.oidc[2].azure.id=123",
        "cas.authn.pac4j.oidc[2].azure.secret=123",
        "cas.authn.pac4j.oidc[2].azure.tenant=contoso.onmicrosoft.com",
        "cas.authn.pac4j.oidc[2].azure.logout-url=https://example.logout",
        "cas.authn.pac4j.oidc[2].azure.discovery-uri=https://localhost:8443/.well-known/openid-configuration",

        "cas.authn.pac4j.oidc[3].keycloak.id=123",
        "cas.authn.pac4j.oidc[3].keycloak.secret=123",
        "cas.authn.pac4j.oidc[3].keycloak.realm=master",
        "cas.authn.pac4j.oidc[3].keycloak.discovery-uri=https://localhost:8443/.well-known/openid-configuration",
        "cas.authn.pac4j.oidc[3].keycloak.base-uri=https://dev-425954.oktapreview.com",

        "cas.authn.pac4j.core.lazy-init=true"
    })
    class OidcClients extends BaseTests {
        @Test
        void verifyClient() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(4, clients.size());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oidc[0].generic.client-name=privateKeyJwtClient",
        "cas.authn.pac4j.oidc[0].generic.id=123",
        "cas.authn.pac4j.oidc[0].generic.secret=123",
        "cas.authn.pac4j.oidc[0].generic.discovery-uri=https://localhost:8443/.well-known/openid-configuration",
        "cas.authn.pac4j.oidc[0].generic.client-authentication-method=private_key_jwt",
        "cas.authn.pac4j.oidc[0].generic.private-key-jwt.jwks.location=classpath:private-key-jwt.jwks",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class PrivateKeyJwtOidcClient extends BaseTests {
        @Test
        void verifyPrivateKeyJwtClientAuthenticationMethod() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());

            val client = (OidcClient) clients.getFirst();
            val config = client.getConfiguration();
            assertEquals(ClientAuthenticationMethod.PRIVATE_KEY_JWT, config.getClientAuthenticationMethod());
            assertNotNull(config.getPrivateKeyJwtClientAuthnMethodConfig());
            val privateKeyJwtConfig = config.getPrivateKeyJwtClientAuthnMethodConfig();
            assertTrue(privateKeyJwtConfig instanceof PrivateKeyJwtClientAuthnMethodConfig);
            val castedPrivateKeyJwtClient = (PrivateKeyJwtClientAuthnMethodConfig) privateKeyJwtConfig;
            assertEquals("class path resource [private-key-jwt.jwks]", castedPrivateKeyJwtClient.getJwks().getJwksResource().toString());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oidc[0].generic.client-name=federatedOidcClient",
        "cas.authn.pac4j.oidc[0].generic.federation.enabled=true",
        "cas.authn.pac4j.oidc[0].generic.federation.jwks.location=classpath:federation.jwks",
        "cas.authn.pac4j.oidc[0].generic.federation.jwks.kid=fedekey-federatedOidcClient",
        "cas.authn.pac4j.oidc[0].generic.federation.validity-in-days=30",
        "cas.authn.pac4j.oidc[0].generic.federation.application-type=web",
        "cas.authn.pac4j.oidc[0].generic.federation.response-types[0]=code",
        "cas.authn.pac4j.oidc[0].generic.federation.grant-types[0]=authorization_code",
        "cas.authn.pac4j.oidc[0].generic.federation.scopes[0]=openid",
        "cas.authn.pac4j.oidc[0].generic.federation.scopes[1]=profile",
        "cas.authn.pac4j.oidc[0].generic.federation.client-registration-types[0]=explicit",
        "cas.authn.pac4j.oidc[0].generic.federation.target-op=https://target-op.example.org",
        "cas.authn.pac4j.oidc[0].generic.federation.trust-anchors[anchor]=https://trust-anchor.example.org/entity",
        "cas.authn.pac4j.oidc[0].generic.federation.contact-name=CAS Team",
        "cas.authn.pac4j.oidc[0].generic.federation.contact-emails[0]=cas@example.org",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class FederationOidcClient extends BaseTests {
        @Test
        void verifyFederationConfigurationAndCustomization() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());

            val client = (OidcClient) clients.getFirst();
            val config = client.getConfiguration();
            val federation = config.getFederation();
            assertTrue(config.isFederation());
            assertEquals("https://target-op.example.org", federation.getTargetOp());
            assertEquals("class path resource [federation.jwks]", federation.getJwks().getJwksResource().toString());
            assertEquals("fedekey-federatedOidcClient", federation.getJwks().getKid());
            assertEquals(1, federation.getTrustAnchors().size());
            assertEquals(30, federation.getValidityInDays());
            assertEquals("web", federation.getApplicationType());
            assertEquals(List.of("code"), federation.getResponseTypes());
            assertEquals(List.of("authorization_code"), federation.getGrantTypes());
            assertEquals(List.of("openid", "profile"), federation.getScopes());
            assertEquals(List.of("explicit"), federation.getClientRegistrationTypes());
            assertEquals("CAS Team", federation.getContactName());
            assertEquals(List.of("cas@example.org"), federation.getContactEmails());
            assertNotNull(federation.getEntityId());
            assertEquals("https://cas.example.org:8443/cas/rp/federatedOidcClient", federation.getEntityId());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.core.lazy-init=false",
        "cas.authn.pac4j.bitbucket.id=123456",
        "cas.authn.pac4j.bitbucket.secret=secret",
        "cas.authn.pac4j.dropbox.id=123456",
        "cas.authn.pac4j.dropbox.secret=secret"
    })
    class EagerInitialization extends BaseTests {
        @Test
        void verifyEagerInit() {
            val clients1 = List.copyOf(delegatedIdentityProviderFactory.build());
            assertEquals(2, clients1.size());
            val clients2 = List.copyOf(delegatedIdentityProviderFactory.build());
            assertFalse(clients2.stream()
                .allMatch(c2 -> clients1.stream().anyMatch(client -> client.hashCode() == c2.hashCode())));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.facebook.id=123456",
        "cas.authn.pac4j.facebook.secret=secret",
        "cas.authn.pac4j.facebook.fields=field1,field2",
        "cas.authn.pac4j.facebook.scope=scope1",

        "cas.authn.pac4j.foursquare.id=123456",
        "cas.authn.pac4j.foursquare.secret=secret",

        "cas.authn.pac4j.linked-in.id=123456",
        "cas.authn.pac4j.linked-in.secret=secret",
        "cas.authn.pac4j.linked-in.scope=scope1",

        "cas.authn.pac4j.google.id=123456",
        "cas.authn.pac4j.google.secret=secret",
        "cas.authn.pac4j.google.scope=EMAIL_AND_PROFILE",

        "cas.authn.pac4j.paypal.id=123456",
        "cas.authn.pac4j.paypal.secret=secret",

        "cas.authn.pac4j.twitter.id=123456",
        "cas.authn.pac4j.twitter.secret=secret",

        "cas.authn.pac4j.github.id=123456",
        "cas.authn.pac4j.github.secret=secret",

        "cas.authn.pac4j.bitbucket.id=123456",
        "cas.authn.pac4j.bitbucket.secret=secret",

        "cas.authn.pac4j.dropbox.id=123456",
        "cas.authn.pac4j.dropbox.secret=secret",

        "cas.authn.pac4j.windows-live.id=123456",
        "cas.authn.pac4j.windows-live.secret=secret",

        "cas.authn.pac4j.wordpress.id=123456",
        "cas.authn.pac4j.wordpress.secret=secret",

        "cas.authn.pac4j.hi-org-server.id=123456",
        "cas.authn.pac4j.hi-org-server.secret=secret",
        "cas.authn.pac4j.hi-org-server.scope=scope1",

        "cas.authn.pac4j.yahoo.id=123456",
        "cas.authn.pac4j.yahoo.secret=secret",

        "cas.authn.pac4j.core.lazy-init=true"
    })
    class IdentifiableClients extends BaseTests {
        @Test
        void verifyFactoryForIdentifiableClients() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(13, clients.size());
        }
    }
    
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.bitbucket.id=123456",
        "cas.authn.pac4j.bitbucket.secret=secret",
        "cas.authn.pac4j.dropbox.id=123456",
        "cas.authn.pac4j.dropbox.secret=secret",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class LazyInitialization extends BaseTests {
        @Test
        void verifyLaziness() {
            val clients1 = List.copyOf(delegatedIdentityProviderFactory.build());
            assertEquals(2, clients1.size());
            val clients2 = List.copyOf(delegatedIdentityProviderFactory.build());
            assertTrue(clients2.stream().allMatch(c2 -> clients1.stream()
                .anyMatch(client -> client.hashCode() == c2.hashCode())));
        }
    }
}
