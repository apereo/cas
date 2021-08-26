package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePublicKey;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAttributeDefinitionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=cas-user-id",
        "cas.authn.attribute-repository.stub.attributes.givenName=cas-given-name",
        "cas.authn.attribute-repository.stub.attributes.eppn=casuser",
        "cas.authn.attribute-repository.stub.attributes.mismatchedAttributeKey=someValue",
        "cas.server.scope=cas.org",
        "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/basic-attribute-definitions.json"
    })
@Tag("Attributes")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DefaultAttributeDefinitionStoreTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultAttributeDefinitionStoreTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private IPersonAttributeDao attributeRepository;

    @Test
    public void verifyReturnAll() {
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);

        val policy = new ReturnAllAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal(person.getAttributes()),
            CoreAuthenticationTestUtils.getService(), CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
        assertTrue(List.class.cast(attributes.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")).contains("cas-user-id@cas.org"));
    }

    @Test
    public void verifyMappedToMultipleNames() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("cn")
            .name("commonName,common-name,cname")
            .build();
        store.registerAttributeDefinition(defn);
        val attrs = store.resolveAttributeValues(CoreAuthenticationTestUtils.getAttributes(), CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(attrs.isEmpty());
        assertFalse(attrs.containsKey("cn"));
        assertTrue(attrs.containsKey("commonName"));
        assertTrue(attrs.containsKey("common-name"));
        assertTrue(attrs.containsKey("cname"));
    }

    @Test
    public void verifyEncryptedAttributeDefinitions() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val servicePublicKey = new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA");
        when(service.getPublicKey()).thenReturn(servicePublicKey);

        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("cn")
            .scoped(true)
            .encrypted(true)
            .build();
        store.registerAttributeDefinition(defn);
        assertTrue(store.locateAttributeDefinition("cn", DefaultAttributeDefinition.class).isPresent());
        assertFalse(store.locateAttributeDefinition("unknown", DefaultAttributeDefinition.class).isPresent());
        val attrs = store.resolveAttributeValues(CoreAuthenticationTestUtils.getAttributes(), service);
        assertFalse(attrs.isEmpty());
        assertTrue(attrs.containsKey("cn"));
        val values = CollectionUtils.toCollection(attrs.get("cn"));
        assertFalse(values.stream()
            .anyMatch(value -> value.toString().equalsIgnoreCase(CoreAuthenticationTestUtils.CONST_USERNAME)));
    }

    @Test
    public void verifyPredicateAttributeDefinitions() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("cn")
            .scoped(true)
            .build();
        store.registerAttributeDefinition(defn);
        assertTrue(store.locateAttributeDefinition(attributeDefinition -> attributeDefinition.equals(defn)).isPresent());
    }

    @Test
    public void verifyMismatchedKeyReturnAll() {
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);

        val policy = new ReturnAllAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal(person.getAttributes()),
            CoreAuthenticationTestUtils.getService(), CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("interesting-attribute"));
        assertTrue(List.class.cast(attributes.get("interesting-attribute")).contains("cas-given-name@cas.org"));
    }

    @Test
    public void verifyAttrDefnNotFound() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("invalid")
            .scoped(true)
            .build();
        store.registerAttributeDefinition(defn);
        var values = (Optional<Pair<AttributeDefinition, List<Object>>>) store.resolveAttributeValues("whatever",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service, Map.of());
        assertTrue(values.isEmpty());
    }

    @Test
    public void verifyAttributeDefinitionsAsMap() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("cn")
            .scoped(true)
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        store.registerAttributeDefinition(defn);
        assertFalse(store.isEmpty());
        val attrs = store.resolveAttributeValues(CoreAuthenticationTestUtils.getAttributes(), service);
        assertFalse(attrs.isEmpty());
        assertTrue(attrs.containsKey("mail"));
        assertTrue(attrs.containsKey(defn.getName()));
        val values = (List<Object>) attrs.get(defn.getName());
        assertTrue(values.contains("TEST@example.org"));
    }

    @Test
    public void verifyScopedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service, Map.of());
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("test@example.org"));
    }

    @Test
    public void verifyScriptedEmbeddedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .script("groovy { logger.info(\" name: ${attributeName}, values: ${attributeValues} \"); return ['hello', 'world'] } ")
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service, Map.of());
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("hello@example.org"));
        assertTrue(values.get().getValue().contains("world@example.org"));
    }

    @Test
    public void verifyScriptedExternalAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("system.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .script("classpath:/attribute-definition.groovy")
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service, Map.of());
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("casuser@system.org"));
        assertTrue(values.get().getValue().contains("groovy@system.org"));
    }

    @Test
    public void verifyFormattedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("givenName")
            .scoped(true)
            .patternFormat("hello,{0}")
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service, Map.of());
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("hello,test@example.org"));
    }

    @Test
    public void verifyOperation() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();

        store.registerAttributeDefinition(defn);
        assertNotNull(store.locateAttributeDefinition("eduPersonPrincipalName"));
        assertFalse(store.getAttributeDefinitions().isEmpty());
    }

    @Test
    public void verifySerialization() throws Exception {
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        MAPPER.writeValue(JSON_FILE, defn);
        val read = MAPPER.readValue(JSON_FILE, AttributeDefinition.class);
        assertEquals(read, defn);
    }

    @Test
    public void verifyStoreSerialization() throws Exception {
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        val store = new DefaultAttributeDefinitionStore(defn);
        store.setScope("example.org");
        val file = File.createTempFile("attr", "json");
        store.to(file);
        assertTrue(file.exists());
        val store2 = new DefaultAttributeDefinitionStore(new FileSystemResource(file));
        assertEquals(store2, store);
    }

    @Test
    public void verifyExternalImport() throws Exception {
        val store = new DefaultAttributeDefinitionStore(new ClassPathResource("AttributeDefns.json"));
        assertFalse(store.getAttributeDefinitions().isEmpty());
        assertNotNull(store.locateAttributeDefinition("eduPersonPrincipalName"));
    }

    @Test
    public void verifyDefinitions() {
        val defn1 = DefaultAttributeDefinition.builder()
            .key("cn")
            .encrypted(true)
            .build();
        val defn2 = DefaultAttributeDefinition.builder()
            .key("cn")
            .build();
        assertEquals(0, defn1.compareTo(defn2));

        val store = new DefaultAttributeDefinitionStore(defn1);
        store.setScope("example.org");

        val service = CoreAuthenticationTestUtils.getRegisteredService();
        var results = store.resolveAttributeValues("cn", List.of("common-name"), service, Map.of());
        assertFalse(results.isEmpty());
        assertTrue(results.get().getValue().isEmpty());

        when(service.getPublicKey()).thenReturn(mock(RegisteredServicePublicKey.class));
        results = store.resolveAttributeValues("cn", List.of("common-name"), service, Map.of());
        assertTrue(results.get().getValue().isEmpty());
    }

    @Test
    public void verifyDefinitionsReload() {
        val resource = casProperties.getAuthn().getAttributeRepository().getAttributeDefinitionStore().getJson().getLocation();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val store = new DefaultAttributeDefinitionStore(resource);
                store.setScope("example.org");
                Files.setLastModifiedTime(resource.getFile().toPath(), FileTime.from(Instant.now()));
                Thread.sleep(5_000);
                store.destroy();
            }
        });
    }

    @Test
    public void verifyBadDefinitionsResource() throws Exception {
        val file = File.createTempFile("badfile", ".json");
        FileUtils.write(file, "data", StandardCharsets.UTF_8);
        val store = new DefaultAttributeDefinitionStore(new FileSystemResource(file));
        store.setScope("example.org");
        assertTrue(store.isEmpty());
    }

    @Test
    public void verifyRemoveDefinition() {
        val store = new DefaultAttributeDefinitionStore();
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
