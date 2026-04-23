package org.apereo.cas;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionResolutionContext;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStoreConfigurer;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.authentication.attribute.JsonAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServicePublicKey;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import tools.jackson.databind.ObjectMapper;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JsonAttributeDefinitionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(
    classes = {
        BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
        JsonAttributeDefinitionStoreTests.JsonAttributeDefinitionStoreTestConfiguration.class
    },
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=cas-user-id",
        "cas.authn.attribute-repository.stub.attributes.givenName=cas-given-name",
        "cas.authn.attribute-repository.stub.attributes.memberships=m1,m2,m3,m4",
        "cas.authn.attribute-repository.stub.attributes.eppn=casuser",
        "cas.authn.attribute-repository.stub.attributes.mismatchedAttributeKey=someValue",
        "cas.authn.attribute-repository.stub.attributes.allgroups=someValue",
        "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/basic-attribute-definitions.json",
        "cas.server.scope=cas.org"
    })
@Tag("Attributes")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class JsonAttributeDefinitionStoreTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultAttributeDefinitionStoreTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private PersonAttributeDao attributeRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyFlattenedDefn() throws Throwable {
        val attributes = getAllReleasedAttributesForCasUser();
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("allgroups"));
        assertEquals(List.of("m1/m2/m3/m4"), attributes.get("allgroups"));
    }

    @Test
    void verifyPatternedValues() throws Throwable {
        val attributes = getAllReleasedAttributesForCasUser();
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("affiliations"));
        assertEquals(List.of("admins", "users"), attributes.get("affiliations"));
    }

    @Test
    void verifyReturnAll() throws Throwable {
        val attributes = getAllReleasedAttributesForCasUser();
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
        assertTrue(attributes.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6").contains("cas-user-id@cas.org"));
    }

    private Map<String, List<Object>> getAllReleasedAttributesForCasUser() throws Throwable {
        val person = attributeRepository.getPerson("casuser");
        assertNotNull(person);
        val policy = new ReturnAllAttributeReleasePolicy();
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(person.getAttributes()))
            .build();
        return policy.getAttributes(releasePolicyContext);
    }

    @Test
    void verifyMappedToMultipleNames() {
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("cn")
                .name("commonName,common-name,cname")
                .build();
            store.registerAttributeDefinition(defn);
            val attributes = CoreAuthenticationTestUtils.getAttributes();
            val attrs = store.resolveAttributeValues(attributes.keySet(), attributes,
                CoreAuthenticationTestUtils.getPrincipal(),
                CoreAuthenticationTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService());
            assertFalse(attrs.isEmpty());
            assertFalse(attrs.containsKey("cn"));
            assertTrue(attrs.containsKey("commonName"));
            assertTrue(attrs.containsKey("common-name"));
            assertTrue(attrs.containsKey("cname"));
        }
    }

    @Test
    void verifyAttributeDefinitionExpiration() {
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("givenName")
                .name("gName")
                .expiration("PT2S")
                .build();
            store.registerAttributeDefinition(defn);
            val attributes = CoreAuthenticationTestUtils.getAttributes();
            val attrs = store.resolveAttributeValues(
                attributes.keySet(),
                attributes,
                CoreAuthenticationTestUtils.getPrincipal(),
                CoreAuthenticationTestUtils.getRegisteredService(),
                CoreAuthenticationTestUtils.getService());
            assertTrue(attrs.containsKey("gName"));
            await().untilAsserted(() -> assertTrue(store.locateAttributeDefinition("givenName").isEmpty()));
        }
    }

    @Test
    void verifyJsonDefinitionOverrides() {
        val defn = attributeDefinitionStore.locateAttributeDefinition("eppn").orElseThrow();
        assertEquals("uid", defn.getAttribute());
        assertEquals("urn:oid:1.3.6.1.4.1.5923.1.1.1.6", defn.getName());
        assertTrue(defn.isScoped());
    }

    @Test
    void verifyEncryptedAttributeDefinitions() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val servicePublicKey = new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA");
        when(service.getPublicKey()).thenReturn(servicePublicKey);

        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("cn")
                .scoped(true)
                .encrypted(true)
                .build();
            store.registerAttributeDefinition(defn);
            assertTrue(store.locateAttributeDefinition("cn", DefaultAttributeDefinition.class).isPresent());
            assertFalse(store.locateAttributeDefinition("unknown", DefaultAttributeDefinition.class).isPresent());
            val attributes = CoreAuthenticationTestUtils.getAttributes();
            val attrs = store.resolveAttributeValues(attributes.keySet(), attributes,
                CoreAuthenticationTestUtils.getPrincipal(),
                service, CoreAuthenticationTestUtils.getService());
            assertFalse(attrs.isEmpty());
            assertTrue(attrs.containsKey("cn"));
            val values = CollectionUtils.toCollection(attrs.get("cn"));
            assertFalse(values.stream()
                .anyMatch(value -> value.toString().equalsIgnoreCase(CoreAuthenticationTestUtils.CONST_USERNAME)));
        }
    }

    @Test
    void verifyPredicateAttributeDefinitions() {
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("cn")
                .scoped(true)
                .build();
            store.registerAttributeDefinition(defn);
            assertTrue(store.locateAttributeDefinition(attributeDefinition -> attributeDefinition.equals(defn)).isPresent());
        }
    }

    @Test
    void verifyMismatchedKeyReturnAll() throws Throwable {
        val attributes = getAllReleasedAttributesForCasUser();
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("interesting-attribute"));
        assertTrue(attributes.get("interesting-attribute").contains("cas-given-name@cas.org"));
    }

    @Test
    void verifyAttrDefnNotFound() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .attribute("invalid")
                .scoped(true)
                .build();
            store.registerAttributeDefinition(defn);

            val context = AttributeDefinitionResolutionContext.builder()
                .attributeValues(CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME))
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .registeredService(service)
                .service(CoreAuthenticationTestUtils.getService())
                .attributes(Map.of())
                .build();
            var values = store.resolveAttributeValues("whatever", context);
            assertTrue(values.isEmpty());
        }
    }

    @Test
    void verifyAttributeDefinitionsAsMap() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("cn")
                .scoped(true)
                .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .build();
            store.registerAttributeDefinition(defn);
            assertFalse(store.isEmpty());
            val attributes = CoreAuthenticationTestUtils.getAttributes();
            val attrs = store.resolveAttributeValues(attributes.keySet(), attributes,
                CoreAuthenticationTestUtils.getPrincipal(),
                service, CoreAuthenticationTestUtils.getService());
            assertFalse(attrs.isEmpty());
            assertTrue(attrs.containsKey("mail"));
            assertTrue(attrs.containsKey(defn.getName()));
            val values = (List<Object>) attrs.get(defn.getName());
            assertTrue(values.contains("TEST@example.org"));
        }
    }


    @Test
    void verifyScopedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .attribute("uid")
                .scoped(true)
                .build();
            store.registerAttributeDefinition(defn);
            val context = AttributeDefinitionResolutionContext.builder()
                .attributeValues(CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME))
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .registeredService(service)
                .service(CoreAuthenticationTestUtils.getService())
                .attributes(Map.of())
                .build();
            var values = store.resolveAttributeValues("eduPersonPrincipalName", context);
            assertTrue(values.isPresent());
            assertTrue(values.get().getValue().contains("test@example.org"));
        }
    }

    @Test
    void verifyScriptedEmbeddedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .attribute("uid")
                .scoped(true)
                .script("groovy { logger.info(\" name: ${attributeName}, values: ${attributeValues} \"); return ['hello', 'world'] } ")
                .build();
            store.registerAttributeDefinition(defn);
            val context = AttributeDefinitionResolutionContext.builder()
                .attributeValues(CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME))
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .registeredService(service)
                .service(CoreAuthenticationTestUtils.getService())
                .attributes(Map.of())
                .build();
            var values = store.resolveAttributeValues("eduPersonPrincipalName", context);
            assertTrue(values.isPresent());
            assertTrue(values.get().getValue().contains("hello@example.org"));
            assertTrue(values.get().getValue().contains("world@example.org"));
        }
    }

    @Test
    void verifyScriptedExternalAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("system.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .attribute("uid")
                .scoped(true)
                .script("classpath:/attribute-definition.groovy")
                .build();
            store.registerAttributeDefinition(defn);
            val context = AttributeDefinitionResolutionContext.builder()
                .attributeValues(CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME))
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .registeredService(service)
                .service(CoreAuthenticationTestUtils.getService())
                .attributes(Map.of())
                .build();
            val values = store.resolveAttributeValues("eduPersonPrincipalName", context);
            assertTrue(values.isPresent());
            assertTrue(values.get().getValue().contains("casuser@system.org"));
            assertTrue(values.get().getValue().contains("groovy@system.org"));
        }
    }

    @Test
    void verifyFormattedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .attribute("givenName")
                .scoped(true)
                .patternFormat("hello,{0}")
                .build();
            store.registerAttributeDefinition(defn);
            val context = AttributeDefinitionResolutionContext.builder()
                .attributeValues(CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME))
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .registeredService(service)
                .service(CoreAuthenticationTestUtils.getService())
                .attributes(Map.of())
                .build();
            val values = store.resolveAttributeValues("eduPersonPrincipalName", context);
            assertTrue(values.isPresent());
            assertTrue(values.get().getValue().contains("hello,test@example.org"));
        }
    }

    @Test
    void verifyOperation() {
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .build();

            store.registerAttributeDefinition(defn);
            assertNotNull(store.locateAttributeDefinition("eduPersonPrincipalName"));
            assertFalse(store.getAttributeDefinitions().isEmpty());
        }
    }

    @Test
    void verifySerialization() {
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        MAPPER.writeValue(JSON_FILE, defn);
        val read = MAPPER.readValue(JSON_FILE, AttributeDefinition.class);
        assertEquals(read, defn);
    }

    @Test
    void verifyStoreSerialization() throws Throwable {
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        try (val store = new JsonAttributeDefinitionStore(defn)) {
            store.setScope("example.org");
            val file = Files.createTempFile("attr", "json").toFile();
            val resource = new FileSystemResource(file);
            store.export(resource);
            assertTrue(file.exists());
            try (val store2 = new JsonAttributeDefinitionStore(resource)) {
                assertEquals(store2, store);
                assertEquals(store2, store2);
                assertNotEquals(store2, mock(AttributeDefinitionStore.class));
            }
        }
    }

    @Test
    void verifyExternalImport() throws Throwable {
        try (val store = new JsonAttributeDefinitionStore(new ClassPathResource("AttributeDefns.json"))) {
            assertFalse(store.getAttributeDefinitions().isEmpty());
            val definition = store.locateAttributeDefinition("eduPersonPrincipalName");
            assertNotNull(definition);
        }
    }

    @Test
    void verifySavingDefinitions() throws Throwable {
        val resource = new FileSystemResource(Files.createTempFile("definitions", ".json"));
        try (val store1 = new JsonAttributeDefinitionStore(resource, true)) {
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .build();
            store1.registerAttributeDefinition(defn);
            store1.save();
        }
        try (val store2 = new JsonAttributeDefinitionStore(resource, true)) {
            assertFalse(store2.getAttributeDefinitions().isEmpty());
        }
    }

    @Test
    void verifyDefinitions() {
        val defn1 = DefaultAttributeDefinition.builder()
            .key("cn")
            .encrypted(true)
            .build();
        val defn2 = DefaultAttributeDefinition.builder()
            .key("cn")
            .build();
        assertEquals(0, defn1.compareTo(defn2));

        try (val store = new JsonAttributeDefinitionStore(defn1)) {
            store.setScope("example.org");

            val service = CoreAuthenticationTestUtils.getRegisteredService();
            val context = AttributeDefinitionResolutionContext.builder()
                .attributeValues(List.of("common-name"))
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .registeredService(service)
                .service(CoreAuthenticationTestUtils.getService())
                .attributes(Map.of())
                .build();

            var results = store.resolveAttributeValues("cn", context);
            assertFalse(results.isEmpty());
            assertTrue(results.get().getValue().isEmpty());

            when(service.getPublicKey()).thenReturn(mock(RegisteredServicePublicKey.class));
            results = store.resolveAttributeValues("cn", context);
            assertTrue(results.get().getValue().isEmpty());
        }
    }

    @Test
    void verifyDefinitionsReload() {
        val resource = casProperties.getAuthn().getAttributeRepository().getAttributeDefinitionStore().getJson().getLocation();
        assertDoesNotThrow(() -> {
            try (val store = new JsonAttributeDefinitionStore(resource)) {
                store.setScope("example.org");
                Files.setLastModifiedTime(resource.getFile().toPath(), FileTime.from(Instant.now()));
                Thread.sleep(5_000);
                store.destroy();
            }
        });
    }

    @Test
    void verifyBadDefinitionsResource() throws Throwable {
        val file = Files.createTempFile("badfile", ".json").toFile();
        FileUtils.write(file, "data", StandardCharsets.UTF_8);
        try (val store = new JsonAttributeDefinitionStore(new FileSystemResource(file))) {
            store.setScope("example.org");
            assertTrue(store.isEmpty());
        }
    }

    @Test
    void verifyRemoveDefinition() {
        try (val store = new JsonAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition.builder()
                .key("eduPersonPrincipalName")
                .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .build();

            store.registerAttributeDefinition(defn);
            assertNotNull(store.locateAttributeDefinition(defn.getKey()));
            assertFalse(store.getAttributeDefinitions().isEmpty());
            store.removeAttributeDefinition(defn.getKey());
            assertTrue(store.locateAttributeDefinition(defn.getKey()).isEmpty());
        }
    }

    @Test
    void verifyLocateAttributeDefnByName() {
        assertTrue(attributeDefinitionStore.locateAttributeDefinitionByName("interesting-attribute", AttributeDefinition.class).isPresent());
    }


    @TestConfiguration(proxyBeanMethods = false)
    static class JsonAttributeDefinitionStoreTestConfiguration {
        @Bean
        public AttributeDefinitionStoreConfigurer testAttributeDefinitionStoreConfigurer() {
            return () ->
                Map.of("eppn", DefaultAttributeDefinition
                    .builder()
                    .key("eppn")
                    .attribute("cn")
                    .scoped(true)
                    .build());

        }
    }
}
