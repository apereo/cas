package org.apereo.cas.web.saml2;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.attributes.GroovyAttributeConverter;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.DefaultSAML2MetadataSigner;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedSaml2IdentityProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */

@Tag("SAML2Web")
class DelegatedSaml2IdentityProviderTests {

    @SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
        properties = "cas.custom.properties.delegation-test.enabled=false")
    @ExtendWith(CasTestExtension.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier("pac4jDelegatedClientFactory")
        protected DelegatedIdentityProviderFactory delegatedIdentityProviderFactory;
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata-aggregate.xml",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-aggregate=true",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp2.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.core.lazy-init=false"
    })
    @Import(SamlMessageStoreTestConfiguration.class)
    class Saml2ClientsWithAggregateIdentityProviders extends BaseTests {
        @Test
        void verifyClient() {
            val clients = delegatedIdentityProviderFactory.build().stream().map(SAML2Client.class::cast).toList();
            assertEquals(2, clients.size());
            assertTrue(clients.stream().anyMatch(c ->
                c.getCustomProperties().get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME).equals("Lafayette College")));
            assertTrue(clients.stream().anyMatch(c ->
                c.getCustomProperties().get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME).equals("Cornell University")));

            val metadataResolver = clients.getFirst().getConfiguration().getIdentityProviderMetadataResolver();
            assertNotNull(metadataResolver.getEntityId());
            assertNotNull(metadataResolver.getMetadata());
            assertNotNull(metadataResolver.getEntityDescriptorElement());
        }
    }

    @Nested
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
    class Saml2ClientsWithCustomMessageStore extends BaseTests {
        @Test
        void verifyClient() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
            val client = (SAML2Client) clients.iterator().next();
            assertNotNull(client.getConfiguration().getSamlMessageStoreFactory());
            assertInstanceOf(DefaultSAML2MetadataSigner.class, client.getConfiguration().getMetadataSigner());
        }
    }

    @Nested
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
    class Saml2ClientsWithUnknownMessageStore extends BaseTests {
        @Test
        void verifyClient() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].saml2-attribute-converter=classpath:/SAMLAttributeConverter.groovy",
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].metadata-signer-strategy=xmlsec",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class Saml2ClientsWithGroovyAttributeConverter extends BaseTests {
        @Test
        void verifyClient() {
            val saml2clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, saml2clients.size());
            val client = (SAML2Client) saml2clients.stream().findFirst().orElseThrow();
            assertInstanceOf(GroovyAttributeConverter.class, client.getConfiguration().getSamlAttributeConverter());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.saml[0].saml2-attribute-converter=org.apereo.cas.web.saml2.DelegatedSaml2IdentityProviderTests.CustomAttributeConverterForTest",
        "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[0].keystore-password=1234567890",
        "cas.authn.pac4j.saml[0].private-key-password=1234567890",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
        "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid",
        "cas.authn.pac4j.saml[0].metadata-signer-strategy=xmlsec",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class Saml2ClientsWithCustomAttributeConverter extends BaseTests {
        @Test
        void verifyClient() {

            val saml2clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, saml2clients.size());

            val client = (SAML2Client) saml2clients.stream().findFirst().get();
            assertInstanceOf(CustomAttributeConverterForTest.class, client.getConfiguration().getSamlAttributeConverter());
        }
    }

    @Nested
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

        "cas.authn.pac4j.saml[1].message-store-factory=org.pac4j.saml.store.HttpSessionStoreFactory",
        "cas.authn.pac4j.saml[1].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
        "cas.authn.pac4j.saml[1].keystore-password=2234567890",
        "cas.authn.pac4j.saml[1].private-key-password=2234567890",
        "cas.authn.pac4j.saml[1].metadata.identity-provider-metadata-path=https://idp.unicon.net/idp/shibboleth",
        "cas.authn.pac4j.saml[1].metadata.service-provider.file-system.location=file:/tmp/sp2.xml",
        "cas.authn.pac4j.saml[1].service-provider-entity-id=test-entityid2",

        "cas.authn.pac4j.core.lazy-init=false"
    })
    class Saml2Clients extends BaseTests {
        @Test
        void verifyClient() {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(2, clients.size());
            clients.forEach(client -> {
                val saml2Client = (SAML2Client) client;
                assertTrue(saml2Client.isInitialized());
                assertInstanceOf(HttpSessionStoreFactory.class, saml2Client.getConfiguration().getSamlMessageStoreFactory());
                assertNotNull(saml2Client.getIdentityProviderMetadataResolver().getEntityId());
            });
        }
    }

    public static class CustomAttributeConverterForTest implements AttributeConverter {
        @Override
        public Object convert(final Object o) {
            return null;
        }
    }

    @TestConfiguration(value = "SamlMessageStoreTestConfiguration", proxyBeanMethods = false)
    static class SamlMessageStoreTestConfiguration {
        @Bean
        public SAMLMessageStoreFactory delegatedSaml2ClientSAMLMessageStoreFactory() {
            return mock(SAMLMessageStoreFactory.class);
        }
    }
}
