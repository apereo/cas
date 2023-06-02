package org.apereo.cas.support.pac4j.clients;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.DefaultSAML2MetadataSigner;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Delegation")
public class DefaultDelegatedClientFactoryTests {

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.core.lazy-init=false",
        "cas.authn.pac4j.bitbucket.id=123456",
        "cas.authn.pac4j.bitbucket.secret=secret",
        "cas.authn.pac4j.dropbox.id=123456",
        "cas.authn.pac4j.dropbox.secret=secret"
    })
    public class EagerInitialization extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyEagerInit() {
            val clients1 = List.copyOf(delegatedClientFactory.build());
            assertEquals(2, clients1.size());
            val clients2 = List.copyOf(delegatedClientFactory.build());
            assertFalse(clients2.stream()
                .allMatch(c2 -> clients1.stream().anyMatch(client -> client.hashCode() == c2.hashCode())));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
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
    public class IdentifiableClients extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyFactoryForIdentifiableClients() {
            val clients = delegatedClientFactory.build();
            assertEquals(13, clients.size());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.github.scope=user",
        "cas.authn.pac4j.github.id=12345",
        "cas.authn.pac4j.github.secret=s3cr3t",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class GitHubClients extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyGithubClient() {
            val clients = delegatedClientFactory.build();
            assertEquals(1, clients.size());
            val client = (GitHubClient) clients.iterator().next();
            assertEquals("user", client.getScope());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oauth2[0].id=123456",
        "cas.authn.pac4j.oauth2[0].secret=s3cr3t",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class OAuth20Clients extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyFactory() {
            val clients = delegatedClientFactory.build();
            assertEquals(1, clients.size());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.cas[0].login-url=https://login.example.org/login",
        "cas.authn.pac4j.cas[0].protocol=SAML",
        "cas.authn.pac4j.cas[0].principal-id-attribute=uid",
        "cas.authn.pac4j.cas[0].css-class=cssClass",
        "cas.authn.pac4j.cas[0].display-name=My CAS",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class CasClients extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyFactoryForCasClientsHavingLoginInDomain() {
            val clients = delegatedClientFactory.build();
            assertEquals(1, clients.size());
            val client = (CasClient) clients.iterator().next();
            assertEquals("https://login.example.org/", client.getConfiguration().getPrefixUrl());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oidc[0].apple.private-key=classpath:apple.pem",
        "cas.authn.pac4j.oidc[0].apple.private-key-id=VB4MYGJ3TQ",
        "cas.authn.pac4j.oidc[0].apple.team-id=67D9XQG2LJ",
        "cas.authn.pac4j.oidc[0].apple.response_type=code id_token",
        "cas.authn.pac4j.oidc[0].apple.response_mode=form_post",
        "cas.authn.pac4j.oidc[0].apple.scope=openid name email",
        "cas.authn.pac4j.oidc[0].apple.id=pac4j",
        "cas.authn.pac4j.oidc[0].apple.use-nonce=true",
        "cas.authn.pac4j.oidc[0].apple.enabled=true",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class AppleClients extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyClient() {
            val clients = delegatedClientFactory.build();
            assertEquals(1, clients.size());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.oidc[0].generic.id=123",
        "cas.authn.pac4j.oidc[0].generic.secret=123",
        "cas.authn.pac4j.oidc[0].generic.response_mode=query",
        "cas.authn.pac4j.oidc[0].generic.response_type=none",
        "cas.authn.pac4j.oidc[0].generic.scope=scope1",
        "cas.authn.pac4j.oidc[0].generic.token-expiration-advance=PT5S",
        "cas.authn.pac4j.oidc[0].generic.preferred-jws-algorithm=RS256",
        "cas.authn.pac4j.oidc[0].generic.discovery-uri=https://dev-425954.oktapreview.com/.well-known/openid-configuration",
        "cas.authn.pac4j.oidc[0].generic.mapped-claims[0]=claim1->attribute1",

        "cas.authn.pac4j.oidc[1].google.id=123",
        "cas.authn.pac4j.oidc[1].google.secret=123",

        "cas.authn.pac4j.oidc[2].azure.id=123",
        "cas.authn.pac4j.oidc[2].azure.secret=123",
        "cas.authn.pac4j.oidc[2].azure.tenant=contoso.onmicrosoft.com",
        "cas.authn.pac4j.oidc[2].azure.logout-url=https://example.logout",

        "cas.authn.pac4j.oidc[3].keycloak.id=123",
        "cas.authn.pac4j.oidc[3].keycloak.secret=123",
        "cas.authn.pac4j.oidc[3].keycloak.realm=master",
        "cas.authn.pac4j.oidc[3].keycloak.base-uri=https://dev-425954.oktapreview.com",

        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class OidcClients extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyClient() {
            val clients = delegatedClientFactory.build();
            assertEquals(4, clients.size());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.bitbucket.id=123456",
        "cas.authn.pac4j.bitbucket.secret=secret",
        "cas.authn.pac4j.dropbox.id=123456",
        "cas.authn.pac4j.dropbox.secret=secret",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class LazyInitialization extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyLaziness() {
            val clients1 = List.copyOf(delegatedClientFactory.build());
            assertEquals(2, clients1.size());
            val clients2 = List.copyOf(delegatedClientFactory.build());
            assertTrue(clients2.stream().allMatch(c2 -> clients1.stream()
                .anyMatch(client -> client.hashCode() == c2.hashCode())));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].message-store-factory=org.pac4j.saml.store.unknown",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    @Import(SamlMessageStoreTestConfiguration.class)
    public class Saml2ClientsWithCustomMessageStore extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyClient() {
            val clients = delegatedClientFactory.build();
            assertEquals(1, clients.size());
            val client = SAML2Client.class.cast(clients.iterator().next());
            assertNotNull(client.getConfiguration().getSamlMessageStoreFactory());
            assertTrue(client.getConfiguration().getMetadataSigner() instanceof DefaultSAML2MetadataSigner);
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].message-store-factory=org.pac4j.saml.store.unknown",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class Saml2ClientsWithUnknownMessageStore extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyClient() {
            val clients = delegatedClientFactory.build();
            assertEquals(1, clients.size());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
            "cas.authn.pac4j.saml[0].saml2AttributeConverter=org.apereo.cas.support.pac4j.clients.DefaultDelegatedClientFactoryTests.CustomAttributeConverterForTest",
            "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
            "cas.authn.pac4j.saml[0].keystore-password=1234567890",
            "cas.authn.pac4j.saml[0].private-key-password=1234567890",
            "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
            "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
            "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
            "cas.authn.pac4j.saml[0].metadata-signer-strategy=xmlsec",
            "cas.authn.pac4j.core.lazy-init=true"
    })
    public class Saml2ClientsWithCustomAttributeConverter extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyClient() {

            val saml2clients = delegatedClientFactory.build();
            assertEquals(1, saml2clients.size());

            val client = (SAML2Client) saml2clients.stream().findFirst().get();
            assertTrue(client.getConfiguration().getSamlAttributeConverter() instanceof CustomAttributeConverterForTest);
        }
    }

    public static class CustomAttributeConverterForTest implements AttributeConverter {

        @Override
        public Object convert(final Object o) {
            return null;
        }
    }

    @TestConfiguration(value = "SamlMessageStoreTestConfiguration", proxyBeanMethods = false)
    public static class SamlMessageStoreTestConfiguration {
        @Bean
        public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory() {
            return mock(SAMLMessageStoreFactory.class);
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].callback-url-type=NONE",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].message-store-factory=org.pac4j.saml.store.HttpSessionStoreFactory",
        "cas.authn.pac4j.saml[0].name-id-policy-format=transient",
        "cas.authn.pac4j.saml[0].mapped-attributes[0]=attr1->givenName",
        "cas.authn.pac4j.saml[0].requested-attributes[0].name=requestedAttribute",
        "cas.authn.pac4j.saml[0].requested-attributes[0].friendly-name=friendlyRequestedName",
        "cas.authn.pac4j.saml[0].blocked-signature-signing-algorithms[0]=sha-1",
        "cas.authn.pac4j.saml[0].signature-algorithms[0]=sha-256",
        "cas.authn.pac4j.saml[0].signature-reference-digest-methods[0]=sha-256",
        "cas.authn.pac4j.saml[0].authn-context-class-ref[0]=classRef1",
        "cas.authn.pac4j.saml[0].assertion-consumer-service-index=1",
        "cas.authn.pac4j.saml[0].principal-id-attribute=givenName",
        "cas.authn.pac4j.saml[0].force-keystore-generation=true",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    public class Saml2Clients extends BaseDelegatedClientFactoryTests {
        @Test
        public void verifyClient() {
            val clients = delegatedClientFactory.build();
            assertEquals(1, clients.size());
            val client = SAML2Client.class.cast(clients.iterator().next());
            assertTrue(client.getConfiguration().getSamlMessageStoreFactory() instanceof HttpSessionStoreFactory);
        }
    }
}
